package jp.com.labit.bukuma.ui.viewholder

import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.toCommaString
import jp.com.labit.bukuma.model.PointTransaction
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by zoonooz on 11/14/2016 AD.
 * View holder for point history list
 */
class PointTransactionViewHolder(parent: ViewGroup) :
    BaseObjectViewHolder<PointTransaction>(parent, R.layout.viewholder_point_transaction) {

  val titleTextView = itemView.findViewById(R.id.title_textview) as TextView
  val timeTextView = itemView.findViewById(R.id.time_textview) as TextView
  val valueTextView = itemView.findViewById(R.id.value_textview) as TextView
  val expiredDayTextView = itemView.findViewById(R.id.expired_day_textview) as TextView

  private val dateFormatString = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

  override fun setObject(obj: PointTransaction) {
    val context = itemView.context

    val expStr = dateFormatString.format(Date(obj.expiredAt * 1000))
    expiredDayTextView.text = context.getString(R.string.point_expiring_day, expStr)
    expiredDayTextView.visibility = View.INVISIBLE

    // time
    val timeStr = dateFormatString.format(Date(obj.createdAt * 1000))
    timeTextView.text = timeStr

    val bookName = obj.merchandise?.title() ?: obj.book?.titleText() ?: ""

    // title
    val title = when (obj.type()) {
      PointTransaction.TYPE_FIRST -> context.getString(R.string.point_transaction_type_first)
      PointTransaction.TYPE_SIGNIN -> context.getString(R.string.point_transaction_type_signin)
      PointTransaction.TYPE_BUY_POINT -> context.getString(R.string.point_transaction_type_buy_point)
      PointTransaction.TYPE_WITHDRAW -> context.getString(R.string.point_transaction_type_withdraw)
      PointTransaction.TYPE_REFUND_BONUS -> {
        expiredDayTextView.visibility = View.VISIBLE
        context.getString(R.string.point_transaction_type_refund_bonus)
      }
      PointTransaction.TYPE_REFUND_NORMAL -> context.getString(R.string.point_transaction_type_refund_normal)
      PointTransaction.TYPE_CAMPAIGN -> context.getString(R.string.point_transaction_type_campaign)
      PointTransaction.TYPE_SOLD_MERCHANDISE -> {
        context.getString(R.string.point_transaction_type_sold_merchandise, "\n$bookName")
      }
      PointTransaction.TYPE_BUY_MERCHANDISE -> {
        if (obj.pointType() == PointTransaction.POINT_TYPE_BONUS)
          context.getString(R.string.point_transaction_type_buy_merchandise_bonus, "\n$bookName")
        else context.getString(R.string.point_transaction_type_buy_merchandise, "\n$bookName")
      }
      PointTransaction.TYPE_ADMIN_BONUS -> {
        if (obj.valueType() == PointTransaction.VALUE_TYPE_PLUS) {
          expiredDayTextView.visibility = View.VISIBLE
          context.getString(R.string.point_transaction_type_admin_bonus_plus)
        } else {
          context.getString(R.string.point_transaction_type_admin_bonus_minus)
        }
      }
      PointTransaction.TYPE_ADMIN_NORMAL -> {
        if (obj.valueType() == PointTransaction.VALUE_TYPE_PLUS)
          context.getString(R.string.point_transaction_type_admin_normal_plus)
        else context.getString(R.string.point_transaction_type_admin_normal_minus)
      }
      PointTransaction.TYPE_EXPIRE -> {
        if (obj.pointType() == PointTransaction.POINT_TYPE_BONUS)
          context.getString(R.string.point_transaction_type_expired_bonus)
        else context.getString(R.string.point_transaction_type_expired_normal)
      }
      PointTransaction.TYPE_BUY_FROM_CARD_LOCAL,
      PointTransaction.TYPE_BUY_FROM_CARD -> {
        context.getString(R.string.point_transaction_type_buy_merchandise_card, "\n$bookName")
      }
      else -> ""
    }

    val prefix: String
    if (obj.valueType() == PointTransaction.VALUE_TYPE_PLUS) {
      valueTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
      prefix = "+"
    } else if (obj.valueType() == PointTransaction.VALUE_TYPE_MINUS) {
      valueTextView.setTextColor(ContextCompat.getColor(context, R.color.red_selected_button))
      prefix = ""
    } else {
      valueTextView.setTextColor(ContextCompat.getColor(context, R.color.black87))
      prefix = ""
    }

    if (obj.pointType() == PointTransaction.POINT_TYPE_BONUS) {
      valueTextView.text = "$prefix${obj.bonusPointChanged.toCommaString()}"
    } else if (obj.pointType() == PointTransaction.POINT_TYPE_NORMAL) {
      valueTextView.text = "$prefix${obj.pointChanged.toCommaString()}"
    } else if (obj.pointType() == PointTransaction.POINT_TYPE_CREDIT) {
      valueTextView.text = "$prefix${obj.creditPoint.toCommaString()}"
    } else {
      valueTextView.text = "0"
    }

    titleTextView.text = title
  }
}