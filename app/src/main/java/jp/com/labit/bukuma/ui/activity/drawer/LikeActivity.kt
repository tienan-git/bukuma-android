package jp.com.labit.bukuma.ui.activity.drawer

import android.os.Bundle
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.ui.activity.BaseActivity
import jp.com.labit.bukuma.ui.fragment.BookLikedFragment

/**
 * Created by zoonooz on 10/18/2016 AD.
 * Book like activity
 */
class LikeActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_container)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = getString(R.string.navigation_like)

    supportFragmentManager.beginTransaction()
        .replace(R.id.container, BookLikedFragment())
        .commit()
  }
}