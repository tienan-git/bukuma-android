package jp.com.labit.bukuma.ui.activity.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import com.jakewharton.rxbinding.view.RxView
import com.jakewharton.rxbinding.widget.RxCompoundButton
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.ui.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_setting.*
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 9/28/2016 AD.
 * Base setting activity. This is the base class to manage setting item list.
 */
abstract class BaseSettingActivity : BaseActivity() {

  lateinit var settings: Array<SettingItem>

  abstract fun settings(): Array<SettingItem>
  open fun onSettingClick(position: Int) {}
  open fun onSettingSwitch(position: Int, value: Boolean) {}

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_setting)
  }

  override fun onResume() {
    super.onResume()
    generateSettingItems()
  }

  /**
   * Re-generate setting UI items from array
   */
  fun generateSettingItems() {
    settings = settings()
    setting_container.removeAllViews()
    for (i in settings.indices) {
      val setting = settings[i]
      val view: View
      if (setting.isHeader) {
        // header -> just set title text
        view = LayoutInflater.from(this).inflate(R.layout.viewholder_header, setting_container, false)
        (view.findViewById(R.id.name) as TextView).text = setting.text
      } else {
        view = LayoutInflater.from(this).inflate(R.layout.setting_item, setting_container, false)
        (view.findViewById(R.id.textview) as TextView).text = setting.text
        // icon
        val iconView = view.findViewById(R.id.icon_imageview) as ImageView
        if (setting.icon != null) {
          iconView.setImageResource(setting.icon)
        } else {
          iconView.visibility = View.GONE
        }
        // disclosure icon
        view.findViewById(R.id.disclosure_icon).visibility = if (setting.showDisclosure) View.VISIBLE else View.GONE

        // state text
        val stateTextView = view.findViewById(R.id.state) as TextView
        if (setting.state != null) {
          stateTextView.text = setting.state
          setting.stateColor?.let { stateTextView.setTextColor(it) }
        } else {
          stateTextView.visibility = View.GONE
        }

        // click event
        RxView.clicks(view).throttleFirst(1, TimeUnit.SECONDS).subscribe { onSettingClick(i) }

        // switch if exist
        val switch = view.findViewById(R.id.switzh) as Switch
        if (setting.switch != null) {
          switch.isChecked = setting.switch
          RxCompoundButton.checkedChanges(switch).skip(1).subscribe { onSettingSwitch(i, it) }
        } else {
          switch.visibility = View.GONE
        }
      }
      setting_container.addView(view)
    }
  }
}

/**
 * Class representing the setting item
 */
data class SettingItem(
    val text: String,
    val icon: Int? = null,
    val isHeader: Boolean = false,
    val showDisclosure: Boolean = true,
    val state: String? = null,
    val switch: Boolean? = null,
    val stateColor: Int? = null
)
