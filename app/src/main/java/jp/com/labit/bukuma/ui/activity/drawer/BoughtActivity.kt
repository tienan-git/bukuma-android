package jp.com.labit.bukuma.ui.activity.drawer

import android.os.Bundle
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.ui.activity.BaseActivity
import jp.com.labit.bukuma.ui.fragment.BookBoughtFragment

/**
 * Created by zoonooz on 10/18/2016 AD.
 * Bought book activity
 */
class BoughtActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_container)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = getString(R.string.navigation_cart)

    supportFragmentManager.beginTransaction()
        .replace(R.id.container, BookBoughtFragment())
        .commit()
  }
}