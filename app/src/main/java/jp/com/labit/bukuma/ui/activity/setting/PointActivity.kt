package jp.com.labit.bukuma.ui.activity.setting

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.ui.activity.drawer.InviteActivity
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by zoonooz on 9/29/2016 AD.
 * Point activity
 */
class PointActivity : BaseSettingActivity() {

  var expiringPoint = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    supportActionBar?.title = getString(R.string.title_point)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
  }

  override fun onResume() {
    super.onResume()
    // refresh user
    service.users.refreshUser().subscribe({
      generateSettingItems()
    }, {})

    // refresh expiring point
    service.api.getExpiringPoint()
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          expiringPoint = it.bonusPoint
          generateSettingItems()
        }, {})
  }

  override fun onSettingClick(position: Int) {
    if (position == 2) {
      startActivity(Intent(this, InviteActivity::class.java))
    }
  }

  override fun settings(): Array<SettingItem> {
    val user = service.currentUser!!
    return arrayOf(
        SettingItem(getString(R.string.point_current), null, false, false,
            getString(R.string.point_unit, user.bonusPoint), null, Color.BLACK),
        SettingItem(getString(R.string.point_expiring), null, false, false,
            getString(R.string.point_unit, expiringPoint), null, Color.RED),
        SettingItem(getString(R.string.point_invite_friend), null, false, true)
    )
  }
}