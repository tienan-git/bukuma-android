package jp.com.labit.bukuma.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.util.copyToClipboard
import jp.com.labit.bukuma.util.infoDialog
import kotlinx.android.synthetic.main.dialog_header.*
import kotlinx.android.synthetic.main.dialog_purchase.*
import java.util.concurrent.TimeUnit

/**
 * Created by zukkey on 2017/06/07.
 * when user purchased, display this.
 */

class PurchasedDialog : BaseDialog() {

  companion object {
    val ARG_TITLE = "arg_title"
    val ARG_DESCRIPTION = "arg_description"
    val ARG_INVITE_TITLE = "arg_invite_title"
    val ARG_CODE_TITLE = "arg_code_title"
    val ARG_CODE = "arg_code"
    val ARG_COPY = "arg_copy"
    val ARG_REVIEW_BUTTON = "arg_review_button"
    val ARG_NEXT_BUTTON = "arg_next_button"
    val ARG_IMAGE_RES_ID = "arg_image_res_id"

    fun newInstance(title: String, desc: String, inviteTitle: String, codeTitle: String,
                    code: String?, copy: String, reviewButton: String, nextButton: String, imageId: Int): PurchasedDialog {
      val fragment = PurchasedDialog()
      val args = Bundle()
      args.putString(ARG_TITLE, title)
      args.putString(ARG_DESCRIPTION, desc)
      args.putString(ARG_INVITE_TITLE, inviteTitle)
      args.putString(ARG_CODE_TITLE, codeTitle)
      args.putString(ARG_CODE, code)
      args.putString(ARG_COPY, copy)
      args.putString(ARG_REVIEW_BUTTON, reviewButton)
      args.putString(ARG_NEXT_BUTTON, nextButton)
      args.putInt(ARG_IMAGE_RES_ID, imageId)
      fragment.arguments = args
      return fragment
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.dialog_purchase, container)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val title = arguments.getString(ARG_TITLE)
    val desc = arguments.getString(ARG_DESCRIPTION)
    val inviteTitle = arguments.getString(ARG_INVITE_TITLE)
    val codeTitle = arguments.getString(ARG_CODE_TITLE)
    val code = arguments.getString(ARG_CODE)
    val copy = arguments.getString(ARG_COPY)
    val reviewButton = arguments.getString(ARG_REVIEW_BUTTON)
    val nextButton = arguments.getString(ARG_NEXT_BUTTON)
    val imageId = arguments.getInt(ARG_IMAGE_RES_ID)

    title_textview.text = title
    desc_textview.text = "$desc\n"
    invite_textView.text = inviteTitle
    code_textView.text = codeTitle
    number_textView.text = code
    copy_textView.text = copy
    invite_button.text = reviewButton
    next_button.text = nextButton
    header_imageview.setImageResource(imageId)

    RxView.clicks(inviteCode).subscribe {
      copyToClipboard(this.context, "code", code)
      infoDialog(this.context, getString(R.string.transaction_id_copied))
    }

    RxView.clicks(invite_button).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      (activity as? PurchasedDialog.PurchasedDialogCallback)?.onInviteButtonClick()
    }

    RxView.clicks(next_button).subscribe {
      (activity as? PurchasedDialog.PurchasedDialogCallback)?.onNextButtonClick()
      dismiss()
    }
  }

  interface PurchasedDialogCallback {
    fun onNextButtonClick()
    fun onInviteButtonClick()
  }
}
