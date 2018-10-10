package jp.com.labit.bukuma.ui.viewholder

import android.view.View
import android.view.ViewGroup
import android.widget.*
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.Merchandise
import jp.com.labit.bukuma.ui.custom.PicassoCircleTransform
import java.lang.Double.parseDouble

/**
 * Created by zoonooz on 10/18/2016 AD.
 * Merchandise View holder
 */
class MerchandiseViewHolder(parent: ViewGroup) :
    BaseObjectViewHolder<Merchandise>(parent, R.layout.viewholder_merchandise) {

  val userLayout = itemView.findViewById(R.id.user_layout) as RelativeLayout
  val merchandiseLayout = itemView.findViewById(R.id.merchandise_layout) as RelativeLayout
  val avatarImageView = itemView.findViewById(R.id.avatar_imageview) as ImageView
  val nameTextView = itemView.findViewById(R.id.name_textview) as TextView
  val happyCountTextView = itemView.findViewById(R.id.mood_happy_textview) as TextView
  val normalCountTextView = itemView.findViewById(R.id.mood_normal_textview) as TextView
  val sadCountTextView = itemView.findViewById(R.id.mood_sad_textview) as TextView
  val commentTextView = itemView.findViewById(R.id.comment_textview) as TextView
  val shippingTextView = itemView.findViewById(R.id.shipping_textview) as TextView
  val priceTextView = itemView.findViewById(R.id.price_textview) as TextView
  val discountTextView = itemView.findViewById(R.id.discount_textview) as TextView
  val statusTextView = itemView.findViewById(R.id.status_textview) as TextView
  val buyButton = itemView.findViewById(R.id.buy_button) as Button
  val imageLayout = itemView.findViewById(R.id.series_image_layout) as LinearLayout
  val series1ImageView = itemView.findViewById(R.id.series1_imageview) as ImageView
  val series2ImageView = itemView.findViewById(R.id.series2_imageview) as ImageView
  val series3ImageView = itemView.findViewById(R.id.series3_imageview) as ImageView
  val officialImageView = itemView.findViewById(R.id.official_merchandise_imageview) as ImageView
  val badgeImageView = itemView.findViewById(R.id.badge_imageview) as ImageView
  val separatorTextView = itemView.findViewById(R.id.separator_textview) as TextView

  fun setObject(obj: Merchandise, isSelf: Boolean) {
    val context = itemView.context
    setObject(obj)

    if (!obj.isSold() && isSelf) {
      buyButton.text = context.getString(R.string.merchandise_button_edit)
      buyButton.setBackgroundResource(R.drawable.background_button_darkgrey)
    }
  }

  override fun setObject(obj: Merchandise) {
    val context = itemView.context
    val user = obj.user
    nameTextView.text = user?.nickname ?: "unknown"
    happyCountTextView.text = "${user?.moodPositive ?: 0}"
    normalCountTextView.text = "${user?.moodSoso ?: 0}"
    sadCountTextView.text = "${user?.moodNegative ?: 0}"
    officialImageView.visibility = View.INVISIBLE
    badgeImageView.visibility = View.INVISIBLE
    separatorTextView.visibility = View.GONE

    val avatarUrl = user?.profileIcon
    if (avatarUrl != null) {
      picasso.load(avatarUrl).fit().centerCrop().transform(PicassoCircleTransform()).into(avatarImageView)
    } else {
      picasso.load(R.drawable.img_thumbnail_user).transform(PicassoCircleTransform()).into(avatarImageView)
    }

    if (obj.isBrandNew) {
      badgeImageView.visibility = View.VISIBLE
      separatorTextView.visibility = View.VISIBLE
    }

    if (user?.isOfficial ?: false) {
      officialImageView.visibility = View.VISIBLE
    }

    if (obj.description.isNotBlank()) {
      commentTextView.visibility = View.VISIBLE
      commentTextView.text = obj.description
    } else {
      commentTextView.visibility = View.GONE
    }

    // ship detail
    shippingTextView.text = obj.shipDetailText(context)

    priceTextView.text = context.getString(R.string.money_unit, obj.price)

    statusTextView.text = obj.qualityText(context)

    if (obj.isSold()) {
      buyButton.text = context.getString(R.string.merchandise_button_sold_out)
      buyButton.setBackgroundResource(R.drawable.background_button_grey)
      discountTextView.visibility = View.INVISIBLE
    } else {
      val listPrice = obj.lintPrice()
      val listPriceDouble = parseDouble(listPrice.toString())
      val publisherPrice = obj.price
      val publisherPriceDouble = parseDouble(publisherPrice.toString())
      val discount = ((1 - publisherPriceDouble / listPriceDouble) * 100).toInt()
      priceTextView.text = context.getString(R.string.money_unit, publisherPrice)
      if(discount >= 30) {
        discountTextView.text = context.getString(R.string.discount, discount)
      } else {
        discountTextView.visibility = View.INVISIBLE
      }
      buyButton.text = context.getString(R.string.merchandise_button_buy)
      buyButton.setBackgroundResource(R.drawable.background_button_red)
    }

    // images
    imageLayout.visibility = View.GONE
    series1ImageView.visibility = if (obj.imageUrl != null) View.VISIBLE else View.GONE
    series2ImageView.visibility = if (obj.image_2Url != null) View.VISIBLE else View.GONE
    series3ImageView.visibility = if (obj.image_3Url != null) View.VISIBLE else View.GONE

    for (i in 0..imageLayout.childCount - 1) {
      if (imageLayout.getChildAt(i).visibility == View.VISIBLE) {
        imageLayout.visibility = View.VISIBLE
        break
      }
    }

    obj.imageUrl?.let { picasso.load(it).into(series1ImageView) }
    obj.image_2Url?.let { picasso.load(it).into(series2ImageView) }
    obj.image_3Url?.let { picasso.load(it).into(series3ImageView) }
  }
}
