package jp.com.labit.bukuma.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.jakewharton.rxbinding.view.RxMenuItem
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.URLSpanCallback
import jp.com.labit.bukuma.extension.stripUnderline
import jp.com.labit.bukuma.util.infoDialog
import jp.com.labit.bukuma.util.loadingDialog
import kotlinx.android.synthetic.main.activity_register_phone.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 9/23/2016 AD.
 * Register phone activity
 */
class RegisterPhoneActivity : BaseActivity(), URLSpanCallback {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_register_phone)

    supportActionBar?.title = getString(R.string.title_register_phone)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    description_textview.stripUnderline(this)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val item = menu.add(R.string.next)
    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

    RxMenuItem.clicks(item)
        .throttleFirst(1, TimeUnit.SECONDS)
        .filter { phone_edittext.text.isNotBlank() }
        .subscribe {
          val progress = loadingDialog(this)
          val phone = phone_edittext.text.replaceFirst(Regex("0"), "+81")
          service.api.updatePhoneNumber(phone)
              .subscribeOn(Schedulers.newThread())
              .observeOn(AndroidSchedulers.mainThread())
              .doOnEach { progress.dismiss() }
              .subscribe({
                // success -> redirect to verify sms activity
                Timber.i("sent phone number to server : $phone")
                startActivity(Intent(this, RegisterSmsActivity::class.java))
                finish()
              }, {
                Timber.e(it, "send phone number error")
                infoDialog(this, getString(R.string.error_tryagain))
              })
        }

    return super.onCreateOptionsMenu(menu)
  }

  override fun onLinkClick(link: String) {
    val intent = Intent(this, WebActivity::class.java)
    if (link == "term") {
      intent.putExtra(WebActivity.EXTRA_URL, getString(R.string.url_term))
    } else {
      intent.putExtra(WebActivity.EXTRA_URL, getString(R.string.url_privacy))
    }
    startActivity(intent)
  }
}
