package jp.com.labit.bukuma.ui.viewholder

import android.view.ViewGroup
import android.widget.TextView
import jp.com.labit.bukuma.R

/**
 * Created by zoonooz on 9/14/2016 AD.
 * Simple action list item holder
 */
class ActionViewHolder(parent: ViewGroup) : BaseObjectViewHolder<String>(parent, R.layout.viewholder_action) {

  val nameTextView = itemView.findViewById(R.id.name) as TextView

  override fun setObject(obj: String) {
    nameTextView.text = obj
  }
}