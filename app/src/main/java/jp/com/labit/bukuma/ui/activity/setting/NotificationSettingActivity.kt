package jp.com.labit.bukuma.ui.activity.setting

import android.os.Bundle
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.api.response.GetNotificationResponse
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber

/**
 * Created by zoonooz on 9/29/2016 AD.
 * Notification setting activity to turn on/off some notification
 */
class NotificationSettingActivity : BaseSettingActivity() {

  var notificationSetting: GetNotificationResponse.Setting? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar?.title = getString(R.string.title_notification_setting)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
  }

  override fun onResume() {
    super.onResume()
    service.api.getNotificationSetting()
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          Timber.i("get notification settings success")
          notificationSetting = it.notificationSetting
          generateSettingItems()
        }, {
          Timber.e(it, "get notification settings error")
        })
  }

  override fun onSettingSwitch(position: Int, value: Boolean) {
    val title = settings[position].text
    Timber.i("setting switch : $title -> $value")
    val notiName = when (title) {
      getString(R.string.notification_setting_book_like) -> "book_like"
      getString(R.string.notification_setting_book_update) -> "book_update"
      getString(R.string.notification_setting_book_sale) -> "book_sale"
      getString(R.string.notification_setting_transaction) -> "transactions"
      getString(R.string.notification_setting_message) -> "message"
      getString(R.string.notification_setting_news) -> "news"
      getString(R.string.notification_setting_email_transaction) -> "transaction_email"
      else -> null
    }

    notiName?.let {
      service.api.setNotificationSetting(it, if (value) 1 else 0)
          .subscribeOn(Schedulers.newThread())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe({
            Timber.i("set notification settings success")
          }, {
            Timber.e(it, "set notification settings error")
          })
    }
  }

  override fun settings(): Array<SettingItem> {
    if (notificationSetting != null) {
      val it = notificationSetting!!
      return arrayOf(
          SettingItem(getString(R.string.notification_setting_title), null, true),
          SettingItem(getString(R.string.notification_setting_book_like), null, false, false, null, it.bookLike),
          SettingItem(getString(R.string.notification_setting_book_update), null, false, false, null, it.bookUpdate),
          SettingItem(getString(R.string.notification_setting_book_sale), null, false, false, null, it.bookSale),
          SettingItem(getString(R.string.notification_setting_transaction), null, false, false, null, it.transactions),
          SettingItem(getString(R.string.notification_setting_message), null, false, false, null, it.message),
          SettingItem(getString(R.string.notification_setting_news), null, false, false, null, it.news),
          SettingItem(getString(R.string.notification_setting_mail_title), null, true),
          SettingItem(getString(R.string.notification_setting_email_transaction), null, false, false, null, it.transactionEmail)
      )
    } else {
      return emptyArray()
    }
  }
}