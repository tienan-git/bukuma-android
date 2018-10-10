package jp.com.labit.bukuma.ui.viewholder

import android.view.ViewGroup
import android.widget.TextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.Book

/**
 * Created by zoonooz on 11/22/2016 AD.
 * search title result
 */
class SearchResultViewHolder(parent: ViewGroup) :
    BaseObjectViewHolder<String>(parent, R.layout.viewholder_search_text) {

  val nameTextView = itemView.findViewById(R.id.name) as TextView

  fun setText(text: String) {
    nameTextView.text = text
  }

  override fun setObject(obj: String) {
    nameTextView.text = obj
  }
}