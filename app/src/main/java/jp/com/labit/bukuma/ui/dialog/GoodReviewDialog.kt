package jp.com.labit.bukuma.ui.dialog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.R
import kotlinx.android.synthetic.main.dialog_good_review.*
import kotlinx.android.synthetic.main.dialog_header.*
import java.util.concurrent.TimeUnit

/**
 * Created by YUTARO SUZUKI on 2017/06/08.
 * when it is good review, display this
 */
class GoodReviewDialog : BaseDialog() {

  companion object {
    val ARG_TITLE = "arg_title"
    val ARG_DESCRIPTION = "arg_description"
    val ARG_REVIEW_TITLE = "arg_review_title"
    val ARG_REVIEW_DESC = "arg_review_desc"
    val ARG_REVIEW_BUTTON = "arg_review_button"
    val ARG_NONE_BUTTON = "arg_none_button"
    val ARG_LATE_BUTTON = "arg_next_button"
    val ARG_IMAGE_RES_ID = "arg_image_res_id"

    fun newInstance(title: String, desc: String, reviewTitle: String, reviewDesc: String,
                    reviewButton: String, noneButton: String, lateButton: String, imageId: Int): GoodReviewDialog {
      val fragment = GoodReviewDialog()
      val args = Bundle()
      args.putString(ARG_TITLE, title)
      args.putString(ARG_DESCRIPTION, desc)
      args.putString(ARG_REVIEW_TITLE, reviewTitle)
      args.putString(ARG_REVIEW_DESC, reviewDesc)
      args.putString(ARG_REVIEW_BUTTON, reviewButton)
      args.putString(ARG_NONE_BUTTON, noneButton)
      args.putString(ARG_LATE_BUTTON, lateButton)
      args.putInt(ARG_IMAGE_RES_ID, imageId)
      fragment.arguments = args
      return fragment
    }
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.dialog_good_review, container)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val title = arguments.getString(ARG_TITLE)
    val desc = arguments.getString(ARG_DESCRIPTION)
    val reviewTitle = arguments.getString(ARG_REVIEW_TITLE)
    val reviewDesc = arguments.getString(ARG_REVIEW_DESC)
    val reviewButton = arguments.getString(ARG_REVIEW_BUTTON)
    val noneButton = arguments.getString(ARG_NONE_BUTTON)
    val lateButton = arguments.getString(ARG_LATE_BUTTON)
    val imageId = arguments.getInt(ARG_IMAGE_RES_ID)

    title_textview.text = title
    desc_textview.text = "$desc\n"
    review_titleTextView.text = reviewTitle
    review_descTextView.text = reviewDesc
    review_button.text = reviewButton
    none_button.text = noneButton
    late_button.text = lateButton
    header_imageview.setImageResource(imageId)

    RxView.clicks(review_button).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=jp.com.labit.bukuma")))
    }

    RxView.clicks(none_button).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      (activity as? GoodReviewDialogCallback)?.onNoneButtonClick()
      dismiss()
    }

    RxView.clicks(late_button).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      (activity as? GoodReviewDialogCallback)?.onLateButtonClick()
      dismiss()
    }
  }

  interface GoodReviewDialogCallback {
    fun onNoneButtonClick()
    fun onLateButtonClick()
  }
}