package jp.com.labit.bukuma.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.R
import kotlinx.android.synthetic.main.dialog_publish.*
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 10/31/2016 AD.
 * Publish done dialog
 */
class PublishedDialog : BaseDialog() {

  companion object {
    val ARG_SHOW_CONTINUE = "arg_show_continue"
    fun newInstance(showContinue: Boolean): PublishedDialog {
      val dialog = PublishedDialog()
      val args = Bundle()
      args.putBoolean(ARG_SHOW_CONTINUE, showContinue)
      dialog.arguments = args
      return dialog
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.dialog_publish, container)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val showContinue = arguments.getBoolean(ARG_SHOW_CONTINUE)
    if (showContinue) {
      RxView.clicks(continue_button).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        (activity as? Callback)?.onContinueClick()
        dismiss()
      }
    } else {
      continue_button.visibility = View.GONE
    }

    RxView.clicks(share_button).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      (activity as? Callback)?.onShareClick()
    }

    RxView.clicks(back_button).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      (activity as? Callback)?.onHomeClick()
      dismiss()
    }
  }

  interface Callback {
    fun onContinueClick()
    fun onHomeClick()
    fun onShareClick()
  }
}
