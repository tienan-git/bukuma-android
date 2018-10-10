package jp.com.labit.bukuma.ui.activity.setting

import android.app.ProgressDialog
import android.os.Bundle
import android.view.Menu
import com.jakewharton.rxbinding.view.RxMenuItem
import jp.com.labit.bukuma.BukumaError
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.Response
import jp.com.labit.bukuma.extension.isValidEmail
import jp.com.labit.bukuma.extension.toResponse
import jp.com.labit.bukuma.ui.activity.BaseActivity
import jp.com.labit.bukuma.util.RxAlertDialog
import jp.com.labit.bukuma.util.infoDialog
import kotlinx.android.synthetic.main.activity_email_update.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 9/28/2016 AD.
 * Activity to update user email
 */
class EmailUpdateActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_email_update)

    supportActionBar?.title = getString(R.string.title_email_update)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    current_email_textview.text = service.currentUser!!.email
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_done, menu)

    // action
    val user = service.currentUser!!
    val progress = ProgressDialog(this)
    progress.setMessage(getString(R.string.loading))
    progress.setCancelable(false)
    RxMenuItem.clicks(menu.findItem(R.id.done))
        .throttleFirst(1, TimeUnit.SECONDS)
        .filter { validate() }
        .doOnNext { progress.show() }
        .flatMap {
          service.users.updateUser(
              new_email_edittext.text.toString(),
              user.nickname,
              user.gender,
              user.biography).toObservable().toResponse()
        }
        .doOnNext {
          progress.dismiss()
          if (it is Response.Error) {
            Timber.e(it.error, "update email error")
            val error = BukumaError.errorType(it.error)
            if (error == BukumaError.Type.HttpError && error.customCode == -203) {
              // email alr taken
              infoDialog(this, getString(R.string.register_error_email_taken))
            } else {
              infoDialog(this, getString(R.string.error_tryagain))
            }
          }
        }
        .filter { it is Response.Success }
        .flatMap {
          RxAlertDialog.alert(this, null,
              getString(R.string.email_update_success_message),
              getString(R.string.ok)).toObservable()
        }
        .subscribe {
          Timber.i("update email success")
          finish()
        }

    return super.onCreateOptionsMenu(menu)
  }

  fun validate(): Boolean {
    var isOk = true

    val new = new_email_edittext.text
    val confirm = confirm_email_edittext.text

    // email
    if (new.isBlank()) {
      isOk = false
      new_email_edittext.error = getString(R.string.register_error_email_empty)
    } else if (!new.toString().isValidEmail()) {
      isOk = false
      new_email_edittext.error = getString(R.string.register_error_email_invalid)
    } else if (new.toString() != confirm.toString()) {
      isOk = false
      confirm_email_edittext.error = getString(R.string.email_update_error_email_notmatch)
    }

    return isOk
  }
}
