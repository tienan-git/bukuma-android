package jp.com.labit.bukuma.ui.activity.drawer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.applyIfLoggedIn
import jp.com.labit.bukuma.ui.activity.WebActivity
import jp.com.labit.bukuma.ui.activity.setting.BaseSettingActivity
import jp.com.labit.bukuma.ui.activity.setting.SettingItem
import kotlinx.android.synthetic.main.activity_setting.*
import timber.log.Timber

/**
 * Created by zukkey on 2017/06/27.
 * Frequently asked question
 */

class FrequentlyQuestionsActivity : BaseSettingActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = getString(R.string.navigation_contact)
  }

  override fun onSettingClick(position: Int) {
    applyIfLoggedIn(this) {
      val title = settings[position].text
      Timber.i("frequently questions clicked: $title, position is $position.")
      when (title) {
        getString(R.string.frequently_questions_freight) -> openWeb(getString(R.string.url_freight))
        getString(R.string.frequently_questions_pay) -> openWeb(getString(R.string.url_pay))
        getString(R.string.frequently_questions_edit_or_delete) -> openWeb(getString(R.string.url_edit_or_delete))
        getString(R.string.frequently_questions_notification) -> openWeb(getString(R.string.url_notification))
        getString(R.string.frequently_questions_evaluation) -> openWeb(getString(R.string.url_evaluation))
        getString(R.string.frequently_questions_cancel) -> openWeb(getString(R.string.url_cancel))
        getString(R.string.frequently_questions_different) -> openWeb(getString(R.string.url_different))
        getString(R.string.frequently_questions_contact) -> startActivity(Intent(this, ReportActivity::class.java))
      }
    }
  }

  override fun onResume() {
    super.onResume()
    generateHeaderItem()
  }

  fun generateHeaderItem() {
    val view = LayoutInflater.from(this).inflate(R.layout.viewholder_header_sub, setting_container, false)
    (view.findViewById(R.id.header_sub_title) as TextView).text = getString(R.string.frequently_questions_header_title)
    (view.findViewById(R.id.header_sub_description) as TextView).text = getString(R.string.frequently_questions_header_description)
    setting_container.addView(view, 0)
  }

  override fun settings(): Array<SettingItem> {
    return arrayOf(
      SettingItem(getString(R.string.frequently_questions_title), null, true),
      SettingItem(getString(R.string.frequently_questions_freight)),
      SettingItem(getString(R.string.frequently_questions_pay)),
      SettingItem(getString(R.string.frequently_questions_edit_or_delete)),
      SettingItem(getString(R.string.frequently_questions_notification)),
      SettingItem(getString(R.string.frequently_questions_evaluation)),
      SettingItem(getString(R.string.frequently_questions_cancel)),
      SettingItem(getString(R.string.frequently_questions_different)),
      SettingItem(getString(R.string.frequently_questions_different_part), null, true),
      SettingItem(getString(R.string.frequently_questions_contact))
    )
  }

  private fun openWeb(url: String) {
    val intent = Intent(this, WebActivity::class.java)
    intent.putExtra(WebActivity.EXTRA_URL, url)
    startActivity(intent)
  }
}
