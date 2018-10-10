package jp.com.labit.bukuma.ui.viewholder

import android.view.ViewGroup
import android.widget.TextView
import jp.com.labit.bukuma.R

/**
 * Created by zoonooz on 11/14/2016 AD.
 * Point transaction header holder
 */
class PointTransactionHeaderViewHolder(parent: ViewGroup) :
    BaseObjectViewHolder<Int>(parent, R.layout.viewholder_point_transaction_header) {

  val titleTextView = itemView.findViewById(R.id.title_textview) as TextView
  val balanceTextView = itemView.findViewById(R.id.balance_textview) as TextView
  val expiringTitleTextView = itemView.findViewById(R.id.expiring_title_textview) as TextView
  val expiringDayTextView = itemView.findViewById(R.id.expiring_day_textview) as TextView
  val expiringPointTextView = itemView.findViewById(R.id.expiring_point_textview) as TextView

  override fun setObject(obj: Int) {
  }
}