package jp.com.labit.bukuma.ui.viewholder

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import jp.com.labit.bukuma.R

/**
 * Created by tani on 6/22/2017 AD.
 * Header for list items
 */
class HeaderWhiteViewHolder(parent: ViewGroup) :
    BaseObjectViewHolder<String>(parent, R.layout.viewholder_header_white) {

  val context: Context = parent.context

  val nameTextView = itemView.findViewById(R.id.name) as TextView

  override fun setObject(obj: String) {
    nameTextView.text = obj
  }

  /**
   * 上部にグレーのマージンを取るかどうか.
   * デフォルトはfalse
   */
  fun setVisibleTopMargin(isVisible: Boolean) {
    (nameTextView.layoutParams as? ViewGroup.MarginLayoutParams)?.let {
      it.topMargin =
          if (isVisible) context.resources.getDimension(R.dimen.header_top_margin).toInt()
          else 0
      nameTextView.layoutParams = it
    }
  }
}
