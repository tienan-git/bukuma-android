package jp.com.labit.bukuma.ui.activity

import android.os.Bundle
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.ui.fragment.BookSearchFragment

/**
 * Created by zoonooz on 10/19/2016 AD.
 * Search result book activity
 */
class SearchResultActivity : BaseActivity() {

  companion object {
    val EXTRA_KEYWORD = "extra_keyword"
    val EXTRA_ORDER_ENABLE = "extra_order_enable"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_container)

    val keyword = intent.getStringExtra(EXTRA_KEYWORD) ?: ""
    val order = intent.getBooleanExtra(EXTRA_ORDER_ENABLE, false)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = keyword

    supportFragmentManager.beginTransaction()
        .replace(R.id.container, BookSearchFragment.newInstance(keyword, order))
        .commit()
  }
}
