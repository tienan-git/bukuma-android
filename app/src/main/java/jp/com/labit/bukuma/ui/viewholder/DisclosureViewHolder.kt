package jp.com.labit.bukuma.ui.viewholder

import android.view.ViewGroup
import android.widget.TextView
import jp.com.labit.bukuma.R

/**
 * Created by zoonooz on 11/7/2016 AD.
 * Item with disclosure icon
 */
class DisclosureViewHolder(parent: ViewGroup) : BaseObjectViewHolder<String>(parent, R.layout.viewholder_disclosure) {

  val titleTextView = itemView.findViewById(R.id.title_textview) as TextView

  override fun setObject(obj: String) {
    titleTextView.text = obj
  }
}