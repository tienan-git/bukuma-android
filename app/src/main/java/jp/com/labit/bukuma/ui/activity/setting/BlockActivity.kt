package jp.com.labit.bukuma.ui.activity.setting

import android.os.Bundle
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.ui.activity.BaseActivity
import jp.com.labit.bukuma.ui.fragment.setting.BlockFragment

/**
 * Created by zoonooz on 10/3/2016 AD.
 * Blocked user list activity
 */
class BlockActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_container)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = getString(R.string.title_block)

    supportFragmentManager.beginTransaction()
        .replace(R.id.container, BlockFragment())
        .commit()
  }
}