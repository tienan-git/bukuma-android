package jp.com.labit.bukuma.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.PointTransaction
import jp.com.labit.bukuma.ui.viewholder.HeaderViewHolder
import jp.com.labit.bukuma.ui.viewholder.PointTransactionHeaderViewHolder
import jp.com.labit.bukuma.ui.viewholder.PointTransactionViewHolder

/**
 * Created by zoonooz on 11/14/2016 AD.
 * Point transaction adapter
 */
class PointTransactionAdapter : BaseAdapter<PointTransaction>() {

  val TYPE_SUM_SALE = 2
  val TYPE_SUM_BONUS = 3
  val TYPE_HEADER = 1
  val TYPE_ITEM = 0

  var sale: Int? = 0
  var bonus: Int? = 0
  var expiringPoint: Int? = 0
  var expiringDayTime: String = ""

  var isNotAppeared: Boolean = false

  override fun getItemCount(): Int {
    return super.getItemCount() + 3
  }

  override fun getItemViewType(position: Int): Int {
    return when (position) {
      0 -> TYPE_SUM_SALE
      1 -> TYPE_SUM_BONUS
      2 -> TYPE_HEADER
      else -> TYPE_ITEM
    }
  }

  override fun getRealItemPosition(position: Int): Int {
    return position - 3
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      TYPE_SUM_SALE, TYPE_SUM_BONUS -> PointTransactionHeaderViewHolder(parent)
      TYPE_HEADER -> HeaderViewHolder(parent)
      else -> PointTransactionViewHolder(parent)
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val context = holder.itemView.context
    if (holder is HeaderViewHolder) {
      holder.setObject(context.getString(R.string.point_transaction_header))
    } else if (holder is PointTransactionViewHolder) {
      val item = getItem(position)
      holder.setObject(item)
    } else if (holder is PointTransactionHeaderViewHolder) {
      if (position == 0) {
        holder.titleTextView.text = context.getString(R.string.point_transaction_balance_sale)
        holder.balanceTextView.text = context.getString(R.string.money_unit, sale)
        holder.expiringTitleTextView.visibility = View.INVISIBLE
        holder.expiringDayTextView.visibility = View.INVISIBLE
        holder.expiringPointTextView.visibility = View.INVISIBLE
      } else if (position == 1) {
        holder.titleTextView.text = context.getString(R.string.point_transaction_balance_bonus)
        holder.balanceTextView.text = context.getString(R.string.point_transaction_balance_point, bonus)
        holder.expiringDayTextView.text = expiringDayTime
        holder.expiringTitleTextView.text = context.getString(R.string.point_transaction_balance_point_title)
        holder.expiringPointTextView.text = context.getString(R.string.point_transaction_balance_point, expiringPoint)
        if (isNotAppeared) {
          holder.expiringTitleTextView.visibility = View.INVISIBLE
          holder.expiringPointTextView.visibility = View.INVISIBLE
        }
      }
    }
  }
}