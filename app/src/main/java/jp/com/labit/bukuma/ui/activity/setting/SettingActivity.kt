package jp.com.labit.bukuma.ui.activity.setting

import android.content.Intent
import android.os.Bundle
import jp.com.labit.bukuma.BuildConfig
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.ui.activity.ProfileEditActivity
import jp.com.labit.bukuma.ui.activity.RegisterPhoneActivity
import jp.com.labit.bukuma.ui.activity.WebActivity
import jp.com.labit.bukuma.util.RxAlertDialog
import jp.com.labit.bukuma.extension.applyIfLoggedIn
import timber.log.Timber

/**
 * Created by zoonooz on 9/16/2016 AD.
 * Setting activity
 */
class SettingActivity : BaseSettingActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = getString(R.string.navigation_setting)
  }

  override fun onSettingClick(position: Int) {
    applyIfLoggedIn(this) {
      val title = settings[position].text
      Timber.i("setting clicked : $title")
      when (title) {
        getString(R.string.setting_user_profile) -> startActivity(Intent(this, ProfileEditActivity::class.java))
        getString(R.string.setting_user_address) -> startActivity(Intent(this, AddressActivity::class.java))
        getString(R.string.setting_user_payment) -> startActivity(Intent(this, CreditCardActivity::class.java))
        getString(R.string.setting_user_info) -> startActivity(Intent(this, SettingEmailActivity::class.java))
        getString(R.string.setting_user_phone) -> updatePhoneNumber()
        getString(R.string.setting_sale_money_request) -> startActivity(Intent(this, MoneyActivity::class.java))
        getString(R.string.setting_sale_point) -> startActivity(Intent(this, PointActivity::class.java))
        getString(R.string.setting_sale_history) -> startActivity(Intent(this, PointTransactionActivity::class.java))
        getString(R.string.setting_other_delete_user) -> startActivity(Intent(this, AccountDeleteActivity::class.java))
        getString(R.string.setting_term_term) -> openWeb(getString(R.string.url_term))
        getString(R.string.setting_term_privacy) -> openWeb(getString(R.string.url_privacy))
        getString(R.string.setting_term_commercial) -> openWeb(getString(R.string.url_commercial))
        getString(R.string.setting_term_licence) -> openWeb(getString(R.string.url_licence))
        getString(R.string.setting_other_pushnoti) -> startActivity(Intent(this, NotificationSettingActivity::class.java))
        getString(R.string.setting_other_block) -> startActivity(Intent(this, BlockActivity::class.java))
      }
    }
  }

  // setting classes

  override fun settings(): Array<SettingItem> {
    val user = service.currentUser
    val phoneStatus = if (user != null && user.verified) {
      user.phoneNumber
    } else {
      getString(R.string.not_done)
    }

    val versionCode = BuildConfig.VERSION_CODE
    val versionName = BuildConfig.VERSION_NAME

    return arrayOf(
        SettingItem(getString(R.string.setting_user_title), null, true),
        SettingItem(getString(R.string.setting_user_profile), R.drawable.ic_set_profile),
        SettingItem(getString(R.string.setting_user_address), R.drawable.ic_set_map),
        SettingItem(getString(R.string.setting_user_payment), R.drawable.ic_set_pay),
        SettingItem(getString(R.string.setting_user_info), R.drawable.ic_set_mail),
        SettingItem(getString(R.string.setting_user_phone), R.drawable.ic_set_phone, false, true, phoneStatus),
        SettingItem(getString(R.string.setting_sale_title), null, true),
        SettingItem(getString(R.string.setting_sale_money_request), R.drawable.ic_set_proceed),
        SettingItem(getString(R.string.setting_sale_point), R.drawable.ic_set_point),
        SettingItem(getString(R.string.setting_sale_history), R.drawable.ic_set_history),
        SettingItem(getString(R.string.setting_term_title), null, true),
        SettingItem(getString(R.string.setting_term_term), R.drawable.ic_set_terms),
        SettingItem(getString(R.string.setting_term_privacy), R.drawable.ic_set_privacy),
        SettingItem(getString(R.string.setting_term_commercial), R.drawable.ic_set_contract),
        SettingItem(getString(R.string.setting_term_licence),R.drawable.ic_set_licence),
        SettingItem(getString(R.string.setting_other_title), null, true),
        SettingItem(getString(R.string.setting_other_pushnoti), R.drawable.ic_set_switch),
        SettingItem(getString(R.string.setting_other_block), R.drawable.ic_set_blocking),
        SettingItem(getString(R.string.setting_other_delete_user), R.drawable.ic_set_exit, false, true),
        SettingItem("Bukuma version $versionName($versionCode)", null, true)
    )
  }

  // action

  private fun updatePhoneNumber() {
    val phone = service.currentUser?.phoneNumber
    if (phone != null) {
      RxAlertDialog.alert2(this, null,
          getString(R.string.setting_phone_update_confirm_message, phone),
          getString(R.string.setting_phone_update_confirm_ok),
          getString(R.string.setting_phone_update_confirm_cancel))
          .filter { it }
          .subscribe { startActivity(Intent(this, RegisterPhoneActivity::class.java)) }
    } else {
      startActivity(Intent(this, RegisterPhoneActivity::class.java))
    }
  }

  private fun openWeb(url: String) {
    val intent = Intent(this, WebActivity::class.java)
    intent.putExtra(WebActivity.EXTRA_URL, url)
    startActivity(intent)
  }
}
