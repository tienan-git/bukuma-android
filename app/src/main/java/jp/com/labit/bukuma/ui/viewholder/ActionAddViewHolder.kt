package jp.com.labit.bukuma.ui.viewholder

import android.view.ViewGroup
import android.widget.TextView
import jp.com.labit.bukuma.R

/**
 * Created by zoonooz on 10/3/2016 AD.
 * Add action view holder
 */
class ActionAddViewHolder(parent: ViewGroup) : BaseObjectViewHolder<String>(parent, R.layout.viewholder_action_add) {

  val titleTextView = itemView.findViewById(R.id.title_textview) as TextView

  override fun setObject(obj: String) {
    titleTextView.text = obj
  }
}
