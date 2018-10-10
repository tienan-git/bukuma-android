package jp.com.labit.bukuma.ui.activity.setting

import android.os.Bundle
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.ui.activity.BaseActivity
import jp.com.labit.bukuma.ui.fragment.setting.CreditCardFragment

/**
 * Created by zoonooz on 10/5/2016 AD.
 * Card list activity
 */
class CreditCardActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_container)

    supportActionBar?.title = getString(R.string.title_creditcard)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    supportFragmentManager.beginTransaction()
        .replace(R.id.container, CreditCardFragment())
        .commit()
  }
}