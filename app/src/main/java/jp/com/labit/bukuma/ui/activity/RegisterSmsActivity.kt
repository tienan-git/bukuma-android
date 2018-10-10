package jp.com.labit.bukuma.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.jakewharton.rxbinding.view.RxMenuItem
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.Response
import jp.com.labit.bukuma.extension.toResponse
import jp.com.labit.bukuma.util.RxAlertDialog
import jp.com.labit.bukuma.util.infoDialog
import jp.com.labit.bukuma.util.loadingDialog
import kotlinx.android.synthetic.main.activity_register_sms.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 9/23/2016 AD.
 * Verify sms
 */
class RegisterSmsActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_register_sms)

    supportActionBar?.title = getString(R.string.title_register_phone)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    RxView.clicks(back_button)
        .throttleFirst(1, TimeUnit.SECONDS)
        .subscribe {
          startActivity(Intent(this,RegisterPhoneActivity::class.java))
          finish()
        }
    RxView.clicks(call_button)
        .throttleFirst(1, TimeUnit.SECONDS)
        .flatMap {
          RxAlertDialog.alert2(this,
              getString(R.string.register_sms_call_request_confirm_dialog_title),
              null,
              getString(R.string.register_sms_call_request_confirm_dialog_button),
              getString(R.string.cancel))
        }
        .filter { it }
        .observeOn(Schedulers.newThread())
        .flatMap { service.api.requestCallToVerify().toObservable().toResponse() }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext { if (it is Response.Error) infoDialog(this, getString(R.string.error_tryagain)) }
        .filter { it is Response.Success }
        .flatMap {
          RxAlertDialog.alert(this,
              getString(R.string.register_sms_call_request_success_dialog_title),
              getString(R.string.register_sms_call_request_success_dialog_message),
              getString(R.string.ok)).toObservable()
        }
        .subscribe { Timber.i("request to verify by phone success") }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val item = menu.add(R.string.done)
    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

    RxMenuItem.clicks(item)
        .throttleFirst(1, TimeUnit.SECONDS)
        .filter { sms_edittext.text.isNotBlank() }
        .subscribe {
          val progress = loadingDialog(this)
          val code = sms_edittext.text.toString()
          service.users.verifyPhoneNumber(code)
              .doOnEach { progress.dismiss() }
              .subscribe({
                Timber.i("verified sms")
                finish()
              }, {
                Timber.e(it, "error verify sms")
                infoDialog(this, getString(R.string.error_tryagain))
              })
        }

    return super.onCreateOptionsMenu(menu)
  }
}
