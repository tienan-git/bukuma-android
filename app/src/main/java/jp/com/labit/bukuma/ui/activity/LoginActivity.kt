package jp.com.labit.bukuma.ui.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import com.jakewharton.rxbinding.view.RxMenuItem
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.*
import jp.com.labit.bukuma.util.infoDialog
import kotlinx.android.synthetic.main.activity_login.*
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 9/26/2016 AD.
 * Email Login activity
 */
class LoginActivity : BaseActivity(), URLSpanCallback {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    supportActionBar?.title = getString(R.string.title_login)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp)

    forget_textview.stripUnderline(this)
    email_form.requestFocus()
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
        .doOnNext { progress.show() }
        .observeOn(Schedulers.newThread())
        .flatMap { service.users.loginWithEmail(email_form.text, password_form.text).toObservable().toResponse() }
        .doOnNext {
          progress.dismiss()
          if (it is Response.Error) {
            Timber.e(it.error, "login by email error")
            infoDialog(this, getString(R.string.error_tryagain))
          }
        }
        .filter { it is Response.Success }
        .subscribe {
          Timber.i("login by email success")
          finish()
        }

    return super.onCreateOptionsMenu(menu)
  }

  fun validate(): Boolean {
    var isOk = true

    // email
    if (email_form.text.isBlank()) {
      isOk = false
      email_form.error = getString(R.string.register_error_email_empty)
    } else if (!email_form.text.isValidEmail()) {
      isOk = false
      email_form.error = getString(R.string.register_error_email_invalid)
    }

    // password
    if (password_form.text.isBlank()) {
      isOk = false
      password_form.error = getString(R.string.register_error_password_empty)
    }

    return isOk
  }

  override fun onLinkClick(link: String) {
    if (link == "forgot") {
      startActivity(Intent(this, LoginResetActivity::class.java))
    }
  }
}
