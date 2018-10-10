package jp.com.labit.bukuma.ui.activity.setting

import android.os.Bundle
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.ui.activity.BaseActivity
import jp.com.labit.bukuma.ui.fragment.setting.PointTransactionFragment

/**
 * Created by zoonooz on 11/14/2016 AD.
 * Point transaction list
 */
class PointTransactionActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_container)
    supportActionBar?.title = getString(R.string.title_point_transaction)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    supportFragmentManager.beginTransaction()
        .replace(R.id.container, PointTransactionFragment())
        .commit()
  }
}