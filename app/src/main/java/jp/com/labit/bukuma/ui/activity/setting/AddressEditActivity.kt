package jp.com.labit.bukuma.ui.activity.setting

import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.view.Menu
import com.jakewharton.rxbinding.view.RxMenuItem
import com.jakewharton.rxbinding.widget.RxTextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.Response
import jp.com.labit.bukuma.extension.isNotKatakana
import jp.com.labit.bukuma.extension.toResponse
import jp.com.labit.bukuma.ui.activity.BaseActivity
import jp.com.labit.bukuma.util.RxAlertDialog
import jp.com.labit.bukuma.util.infoDialog
import kotlinx.android.synthetic.main.activity_address_create.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 10/3/2016 AD.
 * Address edit activity
 */
class AddressEditActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_address_create)

    supportActionBar?.title = getString(R.string.title_address_edit)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    firstname_form.requestFocus()

    // postal

    RxTextView.textChanges(postcode_form.inputView)
        // get postal info if length is 7
        .filter { it.length == 7 }
        .observeOn(Schedulers.newThread())
        .flatMap { service.api.getPostalInfo(it.toString()).toObservable().toResponse() }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          when (it) {
            is Response.Success -> {
              Timber.i("success get postal input : ${postcode_form.text}")
              it.value.postalCode?.let {
                prefecture_form.text = it.prefecture
                city_form.text = it.city
                houseno_form.text = it.area
              }
            }
            is Response.Error -> {
              Timber.e(it.error, "error get postal input : ${postcode_form.text}")
            }
          }
        }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_done, menu)

    val progress = ProgressDialog(this)
    progress.setMessage(getString(R.string.loading))
    progress.setCancelable(false)
    RxMenuItem.clicks(menu.findItem(R.id.done))
        .throttleFirst(1, TimeUnit.SECONDS)
        .filter { validate() }
        .doOnNext { progress.show() }
        .observeOn(Schedulers.newThread())
        .flatMap {
          // name - follow ios logic
          val name = "${firstname_form.text}${lastname_form.text} " +
              "${postcode_form.text}${prefecture_form.text}${city_form.text}" +
              "${houseno_form.text}${service.currentUser!!.id}"
          val personName = "${firstname_form.text} ${lastname_form.text}"
          val personNameKana = "${firstname_kana_form.text} ${lastname_kana_form.text}"

          service.api.createAddress(
              1, name,
              postcode_form.text, prefecture_form.text, "ja",
              city_form.text, houseno_form.text, housename_form.text,
              personName, personNameKana, tel_form.text).toObservable().toResponse()
        }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext {
          progress.dismiss()
          if (it is Response.Error) {
            Timber.e(it.error, "create address failed")
            infoDialog(this, getString(R.string.error_tryagain))
          }
        }
        .filter { it is Response.Success }
        .flatMap {
          RxAlertDialog.alert(
              this, null,
              getString(R.string.address_edit_success_message),
              getString(R.string.ok)).toObservable()
        }
        .subscribe {
          Timber.i("create address success")
          setResult(Activity.RESULT_OK)
          finish()
        }

    return super.onCreateOptionsMenu(menu)
  }

  fun validate(): Boolean {
    var isOk = true

    // firstname
    if (firstname_form.text.isBlank()) {
      isOk = false
      firstname_form.error = getString(R.string.address_edit_error_firstname_empty)
    }

    // lastname
    if (lastname_form.text.isBlank()) {
      isOk = false
      lastname_form.error = getString(R.string.address_edit_error_lastname_empty)
    }

    // first name kana
    if (firstname_kana_form.text.isBlank()) {
      isOk = false
      firstname_kana_form.error = getString(R.string.address_edit_error_firstname_kana_empty)
    } else if (firstname_kana_form.text.isNotKatakana()) {
      isOk = false
      firstname_kana_form.error = getString(R.string.address_edit_error_firstname_kana_invalid)
    }

    // last name kana
    if (lastname_kana_form.text.isBlank()) {
      isOk = false
      lastname_kana_form.error = getString(R.string.address_edit_error_lastname_kana_empty)
    } else if (lastname_kana_form.text.isNotKatakana()) {
      isOk = false
      lastname_kana_form.error = getString(R.string.address_edit_error_lastname_kana_invalid)
    }

    // postal
    if (postcode_form.text.isBlank()) {
      isOk = false
      postcode_form.error = getString(R.string.address_edit_error_postal_empty)
    } else if (postcode_form.text.length != 7) {
      isOk = false
      postcode_form.error = getString(R.string.address_edit_error_postal_invalid)
    }

    // city
    if (city_form.text.isBlank()) {
      isOk = false
      city_form.error = getString(R.string.address_edit_error_city_empty)
    }

    // houseno address
    if (houseno_form.text.isBlank()) {
      isOk = false
      houseno_form.error = getString(R.string.address_edit_error_houseno_empty)
    }

    return isOk
  }
}
