package jp.com.labit.bukuma.ui.activity.setting

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.util.RxAlertDialog
import jp.com.labit.bukuma.util.infoDialog
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber

/**
 * Created by zoonooz on 9/28/2016 AD.
 * Setting activity for email & password
 */
class SettingEmailActivity : BaseSettingActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    supportActionBar?.title = getString(R.string.title_setting_mail)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
  }

  override fun settings() = arrayOf(
      SettingItem(getString(R.string.setting_email_update_title), null, true),
      SettingItem(service.currentUser!!.email),
      SettingItem("*******"),
      SettingItem(getString(R.string.setting_email_reset_title), null, true),
      SettingItem(getString(R.string.setting_email_reset_password), null, false, false)
  )

  override fun onSettingClick(position: Int) {
    when (position) {
      1 -> startActivity(Intent(this, EmailUpdateActivity::class.java))
      2 -> startActivity(Intent(this, PasswordUpdateActivity::class.java))
      4 -> resetPassword()
    }
  }

  fun resetPassword() {
    val progress = ProgressDialog(this)
    progress.setMessage(getString(R.string.loading))
    progress.setCancelable(false)
    RxAlertDialog.alert2(this, null,
        getString(R.string.login_reset_password_confirm_message),
        getString(R.string.ok),
        getString(R.string.cancel))
        .filter { it }
        .doOnEach { progress.show() }
        .observeOn(Schedulers.newThread())
        .flatMap { service.api.resetPassword(service.currentUser!!.email).toObservable() }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnEach { progress.dismiss() }
        .flatMap {
          RxAlertDialog.alert(this,
              getString(R.string.login_reset_password_success_title),
              getString(R.string.login_reset_password_success_message),
              getString(R.string.ok)).toObservable()
        }
        .subscribe({
          Timber.i("reset password requested")
        }, {
          Timber.e(it, "reset password failed")
          infoDialog(this, getString(R.string.error_tryagain))
        })
  }
}
