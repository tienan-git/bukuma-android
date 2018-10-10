package jp.com.labit.bukuma.ui.activity

import android.app.ProgressDialog
import android.os.Bundle
import android.view.Menu
import com.jakewharton.rxbinding.view.RxMenuItem
import jp.com.labit.bukuma.BukumaError
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.Response
import jp.com.labit.bukuma.extension.isValidEmail
import jp.com.labit.bukuma.extension.toResponse
import jp.com.labit.bukuma.util.RxAlertDialog
import jp.com.labit.bukuma.util.infoDialog
import kotlinx.android.synthetic.main.activity_login_reset.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 9/27/2016 AD.
 * Password reset activity
 */
class LoginResetActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login_reset)

    supportActionBar?.title = getString(R.string.title_login_reset)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_done, menu)

    // done action click
    val progress = ProgressDialog(this)
    progress.setMessage(getString(R.string.loading))
    progress.setCancelable(false)
    RxMenuItem.clicks(menu.findItem(R.id.done))
        .throttleFirst(1, TimeUnit.SECONDS)
        .filter { validate() }
        .flatMap { RxAlertDialog.alert2(this, null,
            getString(R.string.login_reset_password_confirm_message),
            getString(R.string.ok),
            getString(R.string.cancel)) }
        .filter { it }
        .doOnEach { progress.show() }
        .observeOn(Schedulers.newThread())
        .flatMap { service.api.resetPassword(email_edittext.text.toString()).toObservable().toResponse() }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext {
          progress.dismiss()
          if (it is Response.Error) {
            Timber.e(it.error, "forgot password error")
            when (BukumaError.errorType(it.error)) {
              BukumaError.Type.HttpError -> infoDialog(this, getString(R.string.login_reset_password_error))
              BukumaError.Type.UnknownError -> infoDialog(this, getString(R.string.error_tryagain))
            }
          }
        }
        .filter { it is Response.Success }
        .flatMap { RxAlertDialog.alert(this,
            getString(R.string.login_reset_password_success_title),
            getString(R.string.login_reset_password_success_message),
            getString(R.string.ok)).toObservable() }
        .subscribe {
          Timber.i("forget password request success")
          finish()
        }

    return super.onCreateOptionsMenu(menu)
  }

  fun validate(): Boolean {
    // email
    if (email_edittext.text.isBlank()) {
      email_edittext.error = getString(R.string.login_reset_password_error_email_empty)
      return false
    } else if (!email_edittext.text.toString().isValidEmail()) {
      email_edittext.error = getString(R.string.login_reset_password_error_email_invalid)
      return false
    }
    return true
  }
}
