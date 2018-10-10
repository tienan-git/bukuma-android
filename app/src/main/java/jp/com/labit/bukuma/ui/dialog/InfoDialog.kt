package jp.com.labit.bukuma.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.R
import kotlinx.android.synthetic.main.dialog_header.*
import kotlinx.android.synthetic.main.dialog_info.*

/**
 * Created by zoonooz on 10/5/2016 AD.
 * Info dialog that will show header image, title, description and button
 */
class InfoDialog : BaseDialog() {

  companion object {
    val ARG_TITLE = "arg_title"
    val ARG_DESCRIPTION = "arg_description"
    val ARG_BUTTON = "arg_button"
    val ARG_IMAGE_RES_ID = "arg_image_res_id"

    fun newInstance(title: String, desc: String, button: String, imageId: Int): InfoDialog {
      val fragment = InfoDialog()
      val args = Bundle()
      args.putString(ARG_TITLE, title)
      args.putString(ARG_DESCRIPTION, desc)
      args.putString(ARG_BUTTON, button)
      args.putInt(ARG_IMAGE_RES_ID, imageId)
      fragment.arguments = args
      return fragment
    }
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.dialog_info, container)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val title = arguments.getString(ARG_TITLE)
    val desc = arguments.getString(ARG_DESCRIPTION)
    val buttonTitle = arguments.getString(ARG_BUTTON)
    val imageId = arguments.getInt(ARG_IMAGE_RES_ID)

    title_textview.text = title
    desc_textview.text = "$desc\n"
    button.text = buttonTitle
    header_imageview.setImageResource(imageId)

    RxView.clicks(button).subscribe {
      (activity as? InfoDialogCallback)?.onButtonClick()
      dismiss()
    }
  }

  interface InfoDialogCallback {
    fun onButtonClick()
  }
}