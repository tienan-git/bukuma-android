package jp.com.labit.bukuma.ui.viewholder

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.toHtmlSpanned
import jp.com.labit.bukuma.model.Merchandise
import jp.com.labit.bukuma.ui.custom.PicassoCircleTransform

/**
 * Created by zoonooz on 11/2/2016 AD.
 * Merchandise view holder for profile
 */
class MerchandiseSellingViewHolder(parent: ViewGroup) :
    BaseObjectViewHolder<Merchandise>(parent, R.layout.viewholder_merchandise_selling) {

  val avatarImageView = itemView.findViewById(R.id.avatar_imageview) as ImageView
  val bookImageView = itemView.findViewById(R.id.book_imageview) as ImageView
  val titleTextView = itemView.findViewById(R.id.textview) as TextView
  val priceTextView = itemView.findViewById(R.id.price_textview) as TextView
  val statusTextView = itemView.findViewById(R.id.quality_textview) as TextView

  override fun setObject(obj: Merchandise) {
    val context = itemView.context

    // image
    val avatar = obj.user?.profileIcon
    if (avatar != null) {
      picasso.load(avatar).transform(PicassoCircleTransform()).fit().centerCrop().into(avatarImageView)
    } else {
      picasso.load(R.drawable.img_thumbnail_user).transform(PicassoCircleTransform()).into(avatarImageView)
    }

    bookImageView.setImageBitmap(null)
    titleTextView.text = context.getString(R.string.book_selling_title, obj.title()).toHtmlSpanned()
    obj.book?.let {
      val cover = it.coverImageUrl
      if (cover != null) {
        picasso.load(cover).into(bookImageView)
      } else {
        picasso.load(R.drawable.img_book_placeholder).into(bookImageView)
      }
    }

    if (obj.isSold()) {
      priceTextView.text = context.getString(R.string.book_selling_sold_out)
      statusTextView.text = ""
      itemView.alpha = 0.5f
    } else {
      priceTextView.text = context.getString(R.string.money_unit, obj.price)
      itemView.alpha = 1f
      val qualityArray = context.resources.getStringArray(R.array.book_status_array)
      statusTextView.text = qualityArray[obj.qualityIndex()]
    }
  }
}
