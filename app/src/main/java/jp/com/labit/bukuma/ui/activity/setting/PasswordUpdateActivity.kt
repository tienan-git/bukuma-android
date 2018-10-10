package jp.com.labit.bukuma.ui.activity.setting

import android.os.Bundle
import android.view.Menu
import com.jakewharton.rxbinding.view.RxMenuItem
import jp.com.labit.bukuma.BukumaError
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.Response
import jp.com.labit.bukuma.extension.toResponse
import jp.com.labit.bukuma.ui.activity.BaseActivity
import jp.com.labit.bukuma.util.RxAlertDialog
import jp.com.labit.bukuma.util.infoDialog
import kotlinx.android.synthetic.main.activity_password_update.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 9/28/2016 AD.
 * Password update activity
 */
class PasswordUpdateActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_password_update)

    supportActionBar?.title = getString(R.string.title_password_update)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_done, menu)

    RxMenuItem.clicks(menu.findItem(R.id.done))
        .throttleFirst(1, TimeUnit.SECONDS)
        .filter { validate() }
        .observeOn(Schedulers.newThread())
        .flatMap {
          service.api.changePassword(
              current_pass_edittext.text.toString(),
              new_pass_edittext.text.toString()).toObservable().toResponse()
        }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext {
          if (it is Response.Error) {
            Timber.e(it.error, "change password failed")
            val type = BukumaError.errorType(it.error)
            if (type == BukumaError.Type.HttpError && type.customCode == -201) {
              infoDialog(this, getString(R.string.password_update_error_incorrect))
            } else {
              infoDialog(this, getString(R.string.error_tryagain))
            }
          }
        }
        .filter { it is Response.Success }
        .flatMap {
          RxAlertDialog.alert(this, null,
              getString(R.string.password_update_success_message),
              getString(R.string.ok)).toObservable()
        }
        .subscribe {
          Timber.i("change password success")
          finish()
        }

    return super.onCreateOptionsMenu(menu)
  }

  fun validate(): Boolean {
    var isOk = true

    val current = current_pass_edittext.text
    val new = new_pass_edittext.text
    val confirm = confirm_pass_edittext.text

    // password
    if (current.isBlank()) {
      isOk = false
      current_pass_edittext.error = getString(R.string.password_update_error_current_empty)
    } else if (new.isBlank()) {
      isOk = false
      new_pass_edittext.error = getString(R.string.password_update_error_new_empty)
    } else if (new.toString() != confirm.toString()) {
      isOk = false
      confirm_pass_edittext.error = getString(R.string.password_update_error_notmatch)
    }

    return isOk
  }
}