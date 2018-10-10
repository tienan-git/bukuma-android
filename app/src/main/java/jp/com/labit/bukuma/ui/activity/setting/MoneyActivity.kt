package jp.com.labit.bukuma.ui.activity.setting

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.ui.activity.WebActivity
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by zoonooz on 9/30/2016 AD.
 * Money request and detail activity
 */
class MoneyActivity : BaseSettingActivity() {

  var expiringPoint = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar?.title = getString(R.string.title_money)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
  }

  override fun onResume() {
    super.onResume()
    service.users.refreshUser().subscribe({
      generateSettingItems()
    }, {})

    // refresh expiring point
    service.api.getExpiringPoint()
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          expiringPoint = it.normalPoint
          generateSettingItems()
        }, {})
  }

  override fun onSettingClick(position: Int) {
    if (position == 2) {
      // request money form
      startActivity(Intent(this, MoneyRequestActivity::class.java))
    } else if(position == 5) {
      val intent = Intent(this, WebActivity::class.java)
      intent.putExtra(WebActivity.EXTRA_TITLE, getString(R.string.money_schedule))
      intent.putExtra(WebActivity.EXTRA_URL, "http://static.bukuma.io/bkm_app/guide/transfer.html#section02")
      startActivity(intent)
    }
  }

  override fun settings(): Array<SettingItem> {
    val user = service.currentUser!!
    return arrayOf(
        SettingItem(getString(R.string.money_current), null, false, false,
            getString(R.string.money_unit, user.point), null, Color.BLACK),
        SettingItem(getString(R.string.money_need_transfer), null, false, false,
            getString(R.string.money_unit, expiringPoint), null, ContextCompat.getColor(this, R.color.colorPrimary)),
        SettingItem(getString(R.string.money_expiring), null, false, true),
        SettingItem(getString(R.string.money_attention), null, true, false,
        null, null, null),
        SettingItem(getString(R.string.money_help), null, true, false,
          null, null, null),
        SettingItem(getString(R.string.money_schedule), null, false, true)
    )
  }
}
