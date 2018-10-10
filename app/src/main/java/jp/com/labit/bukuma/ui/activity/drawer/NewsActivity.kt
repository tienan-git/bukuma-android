package jp.com.labit.bukuma.ui.activity.drawer

import android.os.Bundle
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.ui.activity.BaseActivity
import jp.com.labit.bukuma.ui.fragment.NewsFragment

/**
 * Created by zoonooz on 9/15/2016 AD.
 * News activity
 */
class NewsActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_container)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = getString(R.string.navigation_news)

    supportFragmentManager.beginTransaction()
        .replace(R.id.container, NewsFragment())
        .commit()
  }
}