package jp.com.labit.bukuma.ui.viewholder

import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.Address

/**
 * Created by zoonooz on 10/3/2016 AD.
 * Address view holder
 */
class AddressViewHolder(parent: ViewGroup) : BaseObjectViewHolder<Address>(parent, R.layout.viewholder_address) {

  val addressTextView = itemView.findViewById(R.id.address_textview) as TextView
  val checkImageView = itemView.findViewById(R.id.check_imageview) as ImageView

  override fun setObject(obj: Address) {
    checkImageView.setImageResource(if (obj.default) R.drawable.ic_to_check else 0)
    addressTextView.setTypeface(null, if (obj.default) Typeface.BOLD else Typeface.NORMAL)
    addressTextView.text = obj.addressText()
  }
}
