package jp.com.labit.bukuma.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.com.labit.bukuma.R
import kotlinx.android.synthetic.main.dialog_header.*
import kotlinx.android.synthetic.main.dialog_tag_introduction.*

/**
 * Created by tani on 2017/06/30.
 */
class TagIntroductionDialog: BaseDialog() {

  override fun onCreateView(inflater: LayoutInflater,
                            container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.dialog_tag_introduction, container)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    title_textview.text = context.getText(R.string.dialog_tag_introduction_title)
    desc_textview.text = context.getText(R.string.dialog_tag_introduction_description)
    tag_example_textview.text = context.getText(R.string.dialog_tag_introduction_tag_example)
    header_imageview.setImageResource(R.drawable.img_cover_tag)
  }
}