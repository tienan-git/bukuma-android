package jp.com.labit.bukuma.ui.viewholder

import android.text.format.DateUtils
import android.view.ViewGroup
import android.widget.TextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.Announcement

/**
 * Created by zoonooz on 9/14/2016 AD.
 * News list item holder
 */
class NewsViewHolder(parent: ViewGroup) : BaseObjectViewHolder<Announcement>(parent, R.layout.viewholder_news) {

  val contentTextView = itemView.findViewById(R.id.content_textview) as TextView
  val timeTextView = itemView.findViewById(R.id.time_textview) as TextView

  override fun setObject(obj: Announcement) {
    contentTextView.text = obj.content
    timeTextView.text = DateUtils.getRelativeTimeSpanString((obj.updatedAt * 1000),
        System.currentTimeMillis(),
        0L,
        DateUtils.FORMAT_ABBREV_ALL)
  }
}