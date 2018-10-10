package jp.com.labit.bukuma.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.R
import kotlinx.android.synthetic.main.dialog_delete_alert.*
import java.util.concurrent.TimeUnit

/**
 * Created by zukkey on 2017/06/26.
 * When Bukuma points are left, display this.
 */

class DeleteAlertDialog : BaseDialog() {

  companion object {
    val ARG_TITLE = "arg_title"
    val ARG_DESCRIPTION = "arg_description"
    val ARG_POINT_ALERT = "arg_point_alert"
    val ARG_OK_BUTTON = "arg_ok_button"
    val ARG_NOT_BUTTON = "arg_not_button"

    fun newInstance(title: String, desc: String, point: String, ok_button: String, not_button: String): DeleteAlertDialog {
      val fragment = DeleteAlertDialog()
      val args = Bundle()
      args.putString(ARG_TITLE, title)
      args.putString(ARG_DESCRIPTION, desc)
      args.putString(ARG_POINT_ALERT, point)
      args.putString(ARG_OK_BUTTON, ok_button)
      args.putString(ARG_NOT_BUTTON, not_button)
      fragment.arguments = args
      return fragment
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.dialog_delete_alert, container)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val title = arguments.getString(ARG_TITLE)
    val desc = arguments.getString(ARG_DESCRIPTION)
    val point = arguments.getString(ARG_POINT_ALERT)
    val okButtonTitle = arguments.getString(ARG_OK_BUTTON)
    val notButtonTitle = arguments.getString(ARG_NOT_BUTTON)

    delete_title_textview.text = title
    delete_desc_textview.text = "$desc\n"
    point_textview.text = point
    ok_button.text = okButtonTitle
    not_button.text = notButtonTitle

    RxView.clicks(ok_button).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      (activity as? DeleteAlertDialogCallBack)?.onOkButtonClick()
      dismiss()
    }

    RxView.clicks(not_button).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      dismiss()
    }
  }

  interface DeleteAlertDialogCallBack {
    fun onOkButtonClick()
  }
}