package jp.com.labit.bukuma.ui.viewholder

import android.view.ViewGroup
import android.widget.TextView
import jp.com.labit.bukuma.R

/**
 * Created by zukkey on 2017/04/17.
 * action more look publisher
 */


class ActionMoreLookViewHolder(parent: ViewGroup) : BaseObjectViewHolder<String>(parent, R.layout.viewholder_action_look) {

  val moreLook = itemView.findViewById(R.id.more_look_textview) as TextView

  override fun setObject(obj: String) {
    moreLook.text = obj
  }
}
