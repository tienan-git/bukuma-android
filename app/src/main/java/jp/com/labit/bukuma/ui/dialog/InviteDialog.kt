package jp.com.labit.bukuma.ui.dialog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.ui.activity.MainActivity
import kotlinx.android.synthetic.main.dialog_header.*
import kotlinx.android.synthetic.main.dialog_invite.*
import java.util.concurrent.TimeUnit

/**
 * Created by zukkey on 2017/04/14.
 *
 * After finished trading, invite friend dialog.
 */


class InviteDialog : BaseDialog() {

  companion object {
    val ARG_TITLE = "arg_title"
    val ARG_DESCRIPTION = "arg_description"
    val ARG_BUTTON1 = "arg_button1"
    val ARG_BUTTON2 = "arg_button2"
    val ARG_IMAGE_RES_ID = "arg_image_res_id"

    fun newInstance(title: String, desc: String, button1: String, button2: String, imageId: Int): InviteDialog {
      val fragment = InviteDialog()
      val args = Bundle()
      args.putString(ARG_TITLE, title)
      args.putString(ARG_DESCRIPTION, desc)
      args.putString(ARG_BUTTON1, button1)
      args.putString(ARG_BUTTON2, button2)
      args.putInt(ARG_IMAGE_RES_ID, imageId)
      fragment.arguments = args
      return fragment
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return inflater.inflate(R.layout.dialog_invite, container)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    header_imageview.setImageResource(R.drawable.img_bg_alldone)
    title_textview.text = arguments.getString(InviteDialog.ARG_TITLE)
    desc_textview.text = arguments.getString(InviteDialog.ARG_DESCRIPTION)
    invite_button.text = arguments.getString(InviteDialog.ARG_BUTTON1)
    back_home.text = arguments.getString(InviteDialog.ARG_BUTTON2)

    // invite friend
    RxView.clicks(invite_button).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      (activity as? InviteDialogCallBack)?.onInviteButtonClick()
      dismiss()
    }

    // back home
    RxView.clicks(back_home).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      startActivity(Intent(activity, MainActivity::class.java))
      activity.finish()
      dismiss()
    }
  }

  interface InviteDialogCallBack {
    fun onInviteButtonClick()
  }
}

