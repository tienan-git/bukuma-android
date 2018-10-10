package jp.com.labit.bukuma.ui.activity

import android.os.Bundle
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.ui.fragment.home.ActivitiesFragment

/**
 * Created by zoonooz on 10/6/2016 AD.
 * Activities activity
 */
class ActivitiesActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_container)

    supportActionBar?.title = getString(R.string.title_activities)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    supportFragmentManager.beginTransaction()
        .replace(R.id.container, ActivitiesFragment())
        .commit()
  }
}
