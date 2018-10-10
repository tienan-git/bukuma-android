package jp.com.labit.bukuma.ui.dialog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.ui.activity.MainActivity
import kotlinx.android.synthetic.main.dialog_bad_review.*
import kotlinx.android.synthetic.main.dialog_header.*

/**
 * Created by YUTARO SUZUKI on 2017/06/08.
 * when it is bad review, display this
 */

class BadReviewDialog : BaseDialog() {

  companion object {
    val ARG_TITLE = "arg_title"
    val ARG_DESCRIPTION = "arg_description"
    val ARG_BUTTON = "arg_button"
    val ARG_IMAGE_RES_ID = "arg_image_res_id"

    fun newInstance(title: String, desc: String, backButton: String, imageId: Int): BadReviewDialog {
      val fragment = BadReviewDialog()
      val args = Bundle()
      args.putString(ARG_TITLE, title)
      args.putString(ARG_DESCRIPTION, desc)
      args.putString(ARG_BUTTON, backButton)
      args.putInt(ARG_IMAGE_RES_ID, imageId)
      fragment.arguments = args
      return fragment
    }
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.dialog_bad_review, container)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val title = arguments.getString(ARG_TITLE)
    val desc = arguments.getString(ARG_DESCRIPTION)
    val buttonTitle = arguments.getString(ARG_BUTTON)
    val imageId = arguments.getInt(ARG_IMAGE_RES_ID)

    title_textview.text = title
    desc_textview.text = "$desc\n"
    back_home.text = buttonTitle
    header_imageview.setImageResource(imageId)

    RxView.clicks(back_home).subscribe {
      startActivity(Intent(activity, MainActivity::class.java))
      activity.finish()
      dismiss()
    }
  }
}
