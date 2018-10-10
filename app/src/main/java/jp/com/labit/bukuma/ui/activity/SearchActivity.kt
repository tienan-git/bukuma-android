package jp.com.labit.bukuma.ui.activity

import android.os.Bundle
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.ui.fragment.home.SearchFragment

/**
 * Created by zoonooz on 10/20/2016 AD.
 * Search activity
 */
class SearchActivity : BaseActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_container)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    supportFragmentManager.beginTransaction()
        .replace(R.id.container, SearchFragment.newInstance(true))
        .commit()
  }
}
