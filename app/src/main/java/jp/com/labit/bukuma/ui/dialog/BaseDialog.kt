package jp.com.labit.bukuma.ui.dialog

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.View
import android.view.Window
import com.squareup.picasso.Picasso
import jp.com.labit.bukuma.BukumaApplication
import jp.com.labit.bukuma.BukumaConfig
import jp.com.labit.bukuma.api.BukumaService
import javax.inject.Inject

/**
 * Created by zoonooz on 9/15/2016 AD.
 * Base class for dialog
 * - inject dependencies
 * - remove top space from default title
 */
open class BaseDialog : DialogFragment() {

  @Inject lateinit var service: BukumaService
  @Inject lateinit var picasso: Picasso
  @Inject lateinit var config: BukumaConfig

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    BukumaApplication.mainComponent.inject(this)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    // remove top space
    dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
  }
}
