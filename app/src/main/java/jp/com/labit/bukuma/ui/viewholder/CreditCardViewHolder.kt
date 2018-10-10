package jp.com.labit.bukuma.ui.viewholder

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.CreditCard

/**
 * Created by zoonooz on 10/5/2016 AD.
 * CreditCard payment viewholder
 */
class CreditCardViewHolder(parent: ViewGroup) :
    BaseObjectViewHolder<CreditCard>(parent, R.layout.viewholder_creditcard) {

  var cardImageView = itemView.findViewById(R.id.card_imageview) as ImageView
  var cardNumber = itemView.findViewById(R.id.cardnum_textview) as TextView
  val cardDate = itemView.findViewById(R.id.carddate_textview) as TextView
  val checkImageView = itemView.findViewById(R.id.check_imageview) as ImageView

  override fun setObject(obj: CreditCard) {
    val context = itemView.context
    cardNumber.text = "**** **** **** ${obj.last_4}"
    cardDate.text = context.getString(R.string.payment_creditcard_date, obj.expMonth, obj.expYear)
    checkImageView.setImageResource(if (obj.default) R.drawable.ic_to_check else 0)

    if (obj.info != null && obj.info!!.brand == "MasterCard") {
      cardImageView.setImageResource(R.drawable.img_credit_master)
    } else {
      cardImageView.setImageResource(R.drawable.img_credit_visa)
    }
  }
}