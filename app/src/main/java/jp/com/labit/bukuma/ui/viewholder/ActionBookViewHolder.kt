package jp.com.labit.bukuma.ui.viewholder

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import jp.com.labit.bukuma.R

/**
 * Created by zoonooz on 10/17/2016 AD.
 * Book action
 */
class ActionBookViewHolder(parent: ViewGroup) : BaseObjectViewHolder<String>(parent, R.layout.viewholder_action_book) {

  val titleTextView = itemView.findViewById(R.id.title_textview) as TextView
  val iconImageView = itemView.findViewById(R.id.icon_imageview) as ImageView

  override fun setObject(obj: String) {
    titleTextView.text = obj
  }

  fun setObject(obj: String, icon: Int) {
    setObject(obj)
    iconImageView.setImageResource(icon)
  }
}