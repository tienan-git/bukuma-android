package jp.com.labit.bukuma.ui.activity.setting

import android.os.Bundle
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.ui.activity.BaseActivity
import jp.com.labit.bukuma.ui.fragment.setting.AddressFragment

/**
 * Created by zoonooz on 10/3/2016 AD.
 * Address list activity
 */
class AddressActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_container)

    supportActionBar?.title = getString(R.string.title_address)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    supportFragmentManager.beginTransaction()
        .replace(R.id.container, AddressFragment())
        .commit()
  }
}
