package jp.com.labit.bukuma.ui.activity.setting

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ArrayAdapter
import com.jakewharton.rxbinding.view.RxMenuItem
import com.jakewharton.rxbinding.view.RxView
import com.jakewharton.rxbinding.widget.RxTextView
import jp.com.labit.bukuma.BukumaError
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.Response
import jp.com.labit.bukuma.extension.toResponse
import jp.com.labit.bukuma.ui.activity.BaseActivity
import jp.com.labit.bukuma.ui.activity.drawer.ReportActivity
import jp.com.labit.bukuma.ui.dialog.InfoDialog
import jp.com.labit.bukuma.util.RxAlertDialog
import jp.com.labit.bukuma.util.infoDialog
import kotlinx.android.synthetic.main.activity_creditcard_create.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 10/5/2016 AD.
 * Credit card form
 */
class CreditCardEditActivity : BaseActivity() {

  val years = (2016..2028).toList()
  val months = (1..12).toList()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_creditcard_create)

    supportActionBar?.title = getString(R.string.title_creditcard_title)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    // expiry date + month
    val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
    yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    year_spinner.adapter = yearAdapter
    val monthAdapter = ArrayAdapter(this,
        android.R.layout.simple_spinner_item,
        months.map { String.format("%02d", it) })
    monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    month_spinner.adapter = monthAdapter

    // credit brand image
    card_imageview.visibility = View.GONE
    RxTextView.textChanges(number_edittext).subscribe {
      if (it.startsWith("4")) {
        card_imageview.visibility = View.VISIBLE
        card_imageview.setImageResource(R.drawable.img_credit_visa)
      } else if (it.startsWith("5")) {
        card_imageview.visibility = View.VISIBLE
        card_imageview.setImageResource(R.drawable.img_credit_master)
      } else {
        card_imageview.visibility = View.GONE
      }
    }

    // ccv info
    RxView.clicks(ccv_help_imageview)
        .throttleFirst(1, TimeUnit.SECONDS)
        .subscribe {
          val dialog = InfoDialog.newInstance(
              getString(R.string.payment_creditcard_dialog_ccv_title),
              getString(R.string.payment_creditcard_dialog_ccv_description),
              getString(R.string.ok),
              R.drawable.img_bg_ccv)
          dialog.show(supportFragmentManager, "ccv")
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
        .flatMap { service.api.getAddresses().toObservable().toResponse() }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext {
          if (it is Response.Error) {
            progress.dismiss()
            infoDialog(this, getString(R.string.error_tryagain))
          } else if (it is Response.Success && it.value.userAddresses.size == 0) {
            progress.dismiss()
            RxAlertDialog.alert2(this, null,
                getString(R.string.payment_creditcard_need_address_message),
                getString(R.string.payment_creditcard_need_address_ok),
                getString(R.string.cancel))
                .filter { it }
                .subscribe { startActivity(Intent(this, AddressEditActivity::class.java)) }
          }
        }
        .filter { it is Response.Success && it.value.userAddresses.size > 0 }
        .observeOn(Schedulers.newThread())
        .flatMap {
          service.creditCards.createCreditCard(
              name_edittext.text.toString(),
              number_edittext.text.toString(),
              ccv_edittext.text.toString(),
              years[year_spinner.selectedItemPosition],
              months[month_spinner.selectedItemPosition]).toObservable().toResponse()
        }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext {
          progress.dismiss()
          if (it is Response.Error) {
            Timber.e(it.error, "error create credit card")
            val type = BukumaError.errorType(it.error)
            if (type == BukumaError.Type.HttpError && type.invalidCode == "invalid_card") {
              RxAlertDialog.alert2(this,
                null,
                getString(R.string.payment_creditcard_error_card_invalid),
                getString(R.string.navigation_contact),
                getString(R.string.cancel))
                .filter { it }
                .doOnNext { progress.show() }
                .observeOn(Schedulers.newThread())
                .doOnEach { progress.dismiss() }
                .subscribe({
                  val intent = Intent(this, ReportActivity::class.java)
                  startActivity(intent)
                  finish()
                })
            } else if (type == BukumaError.Type.HttpError && type.errorCode == 400) {
              // credit card invalid
              infoDialog(this, getString(R.string.payment_creditcard_error_card))
            } else {
              infoDialog(this, getString(R.string.error_tryagain))
            }
          }
        }
        .filter { it is Response.Success }
        .flatMap {
          RxAlertDialog.alert(this, null,
              getString(R.string.payment_creditcard_edit_success_message),
              getString(R.string.ok)).toObservable()
        }
        .subscribe {
          Timber.i("success create credit card")
          setResult(Activity.RESULT_OK)
          finish()
        }

    return super.onCreateOptionsMenu(menu)
  }

  fun validate(): Boolean {
    var isOk = true

    if (name_edittext.text.isBlank()) {
      isOk = false
      name_edittext.error = getString(R.string.payment_creditcard_error_name_empty)
    }

    if (number_edittext.text.isBlank()) {
      isOk = false
      number_edittext.error = getString(R.string.payment_creditcard_error_number_empty)
    }

    if (ccv_edittext.text.isBlank()) {
      isOk = false
      ccv_edittext.error = getString(R.string.payment_creditcard_error_ccv_empty)
    }

    return isOk
  }
}
