package jp.com.labit.bukuma.ui.activity.drawer

import android.os.Bundle
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.ui.activity.BaseActivity
import jp.com.labit.bukuma.ui.fragment.BookSellFragment

/**
 * Created by zoonooz on 10/18/2016 AD.
 * Book selling activity
 */
class SellingActivity : BaseActivity() {

  companion object {
    val EXTRA_USER_ID = "extra_user_id"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_container)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = getString(R.string.navigation_sell)

    val userId = intent.getIntExtra(EXTRA_USER_ID, service.currentUser?.id ?: 0)

    supportFragmentManager.beginTransaction()
        .replace(R.id.container, BookSellFragment.newInstance(if (userId == 0) null else userId))
        .commit()
  }
}