package jp.com.labit.bukuma.ui.activity

import android.os.Bundle
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.ui.fragment.home.TodoFragment

/**
 * Created by zoonooz on 10/24/2016 AD.
 * To do list activity
 */
class TodoActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_container)

    supportActionBar?.title = getString(R.string.title_todo)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    supportFragmentManager.beginTransaction()
        .replace(R.id.container, TodoFragment())
        .commit()
  }
}