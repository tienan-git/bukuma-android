package jp.com.labit.bukuma.ui.activity.drawer

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.ArrayRes
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import com.jakewharton.rxbinding.view.RxMenuItem
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.ui.activity.BaseActivity
import jp.com.labit.bukuma.util.getLocalIpAddress
import kotlinx.android.synthetic.main.activity_report.*
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 9/14/2016 AD.
 * Report activity
 */
class ReportActivity : BaseActivity() {

  companion object {
    val EXTRA_OPPONENT_ID = "extra_opponent_id"
    val EXTRA_OPPONENT_NAME = "extra_opponent_name"
    val EXTRA_BOOK_ID = "extra_book_id"
    val EXTRA_BOOK_TITLE = "extra_book_title"
    val EXTRA_TRANSACTION_ID = "extra_transaction_id"
    val EXTRA_TRANSACTION_CODE = "extra_transaction_code"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_report)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = getString(R.string.navigation_contact)

    device_textview.text = Build.MODEL
    os_textview.text = Build.VERSION.RELEASE
    name_textview.text = getString(this.applicationInfo.labelRes)
    contact_form_message.text = config.contactForm
    try {
      version_textview.text = packageManager.getPackageInfo(packageName, 0).versionName
    } catch (ignore: Exception) {
    }

    setTopicSpinnerEntries(R.array.report_topic_array)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val item = menu.add(R.string.send)
    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

    RxMenuItem.clicks(item)
        .throttleFirst(1, TimeUnit.SECONDS)
        .filter { validate() }
        .subscribe {
          val topic = resources.getStringArray(R.array.report_topic_array)[topic_spinner.selectedItemPosition]
          val intent = Intent(Intent.ACTION_SENDTO)
          intent.data = Uri.parse("mailto:")
          intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("bukuma.contact@gmail.com"))
          intent.putExtra(Intent.EXTRA_SUBJECT, "${getString(R.string.app_name)} $topic")
          intent.putExtra(Intent.EXTRA_TEXT, text())
          if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            finish()
          }
        }

    return super.onCreateOptionsMenu(menu)
  }

  private fun setTopicSpinnerEntries(@ArrayRes stringArray: Int) {
    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
        resources.getStringArray(stringArray))
    topic_spinner.adapter = adapter
  }

  fun validate(): Boolean {
    return true
  }

  fun text(): String {
    val body = StringBuilder()
    body.append(description_edittext.text)
    body.append("\n\n")

    // device info
    body.append("Device:\n").append(device_textview.text).append("\n\n")
    body.append("OS:\n").append(os_textview.text).append("\n\n")
    body.append("App:\n").append(name_textview.text).append(" ").append(version_textview.text).append("\n\n")
    body.append("UUID:\n").append(config.uuid).append("\n\n")
    body.append("IP Address:\n").append(getLocalIpAddress()).append("\n\n")
    body.append("Contact Form:\n").append(config.contactForm).append("\n\n")

    service.currentUser?.let {
      body.append("UserID:\n").append(it.id).append("\n\n")
      body.append("UserName:\n").append(it.nickname).append("\n\n")
    }

    val opponentId = intent.getIntExtra(EXTRA_OPPONENT_ID, 0)
    if (opponentId != 0) body.append("OpponentID:\n").append(opponentId).append("\n\n")
    intent.getStringExtra(EXTRA_OPPONENT_NAME)?.let {
      body.append("OpponentName:\n").append(it).append("\n\n")
    }

    val bookId = intent.getIntExtra(EXTRA_BOOK_ID, 0)
    if (bookId != 0) body.append("BookID:\n").append(bookId).append("\n\n")
    intent.getStringExtra(EXTRA_BOOK_TITLE)?.let {
      body.append("BookTitle:\n").append(it).append("\n\n")
    }

    val transactionId = intent.getIntExtra(EXTRA_TRANSACTION_ID, 0)
    if (transactionId != 0) body.append("ItemTransactionID:\n").append(transactionId).append("\n\n")
    intent.getStringExtra(EXTRA_TRANSACTION_CODE)?.let {
      body.append("UniqueID:\n").append(it).append("\n\n")
    }

    return body.toString()
  }
}
