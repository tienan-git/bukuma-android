package jp.com.labit.bukuma.ui.viewholder

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.Merchandise

/**
 * Created by zoonooz on 11/7/2016 AD.
 * Book item viewholder profile
 */
class ProfileBookItemViewHolder(parent: ViewGroup) :
    BaseObjectViewHolder<Merchandise>(parent, R.layout.viewholder_profile_book_item) {

  val coverImageView = itemView.findViewById(R.id.cover_imageview) as ImageView
  val titleTextView = itemView.findViewById(R.id.title_textview) as TextView
  val priceTextView = itemView.findViewById(R.id.price_textview) as TextView

  override fun setObject(obj: Merchandise) {
    val context = itemView.context
    priceTextView.text = context.getString(R.string.money_unit, obj.price)
    titleTextView.text = obj.title()

    obj.book?.let {
      val cover = it.coverImageUrl
      if (cover != null) {
        picasso.load(cover).into(coverImageView)
      } else {
        picasso.load(R.drawable.img_book_placeholder).into(coverImageView)
      }

      // cover size
      val height = context.resources.getDimensionPixelSize(R.dimen.book_profile_height)
      val ratio = height.toFloat() / it.height()
      val width = (it.width() * ratio).toInt()

      val lp = coverImageView.layoutParams
      lp.width = width
      coverImageView.layoutParams = lp
    }
  }
}
