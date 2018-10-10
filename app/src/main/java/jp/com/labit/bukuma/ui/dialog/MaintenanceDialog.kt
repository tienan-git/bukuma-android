package jp.com.labit.bukuma.ui.dialog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.ui.activity.BaseActivity
import jp.com.labit.bukuma.ui.activity.WebActivity
import kotlinx.android.synthetic.main.dialog_header.*
import kotlinx.android.synthetic.main.dialog_info.*
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 2017/01/11.
 * Maintenance dialog
 */
class MaintenanceDialog : BaseDialog() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    isCancelable = false
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.dialog_info, container)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    title_textview.text = getString(R.string.dialog_maintenance_title)
    desc_textview.text = getString(R.string.dialog_maintenance_description)
    button.text = getString(R.string.dialog_maintenance_button)

    RxView.clicks(button).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      if (config.maintenanceUrl.isNotBlank()) {
        val intent = Intent(activity, WebActivity::class.java)
        intent.putExtra(WebActivity.EXTRA_URL, config.maintenanceUrl)
        intent.putExtra(BaseActivity.EXTRA_IGNORE_MAINTENANCE, true)
        startActivity(intent)
      }
    }
  }
}