package jp.com.labit.bukuma.ui.viewholder

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.Merchandise

/**
 * Created by zoonooz on 11/2/2016 AD.
 * Merchandise viewholder compact version
 */
class MerchandiseChooserViewHolder(parent: ViewGroup) :
    BaseObjectViewHolder<Merchandise>(parent, R.layout.viewholder_merchandise_chooser) {

  val imageView = itemView.findViewById(R.id.book_imageview) as ImageView
  val titleTextView = itemView.findViewById(R.id.title_textview) as TextView
  val soldOutTextView = itemView.findViewById(R.id.sold_textview) as TextView
  val priceLayout = itemView.findViewById(R.id.price_layout) as LinearLayout
  val priceTextView = itemView.findViewById(R.id.price_textview) as TextView
  val statusTextView = itemView.findViewById(R.id.quality_textview) as TextView

  override fun setObject(obj: Merchandise) {
    val context = itemView.context

    imageView.setImageBitmap(null)
    titleTextView.text = obj.title()

    val bookCover = obj.book?.coverImageUrl
    val bookWidth = obj.book?.imageWidth ?: 0
    val bookHeight = obj.book?.imageHeight ?: 0

    if (bookCover != null) {
      picasso.load(bookCover).into(imageView)
    } else {
      picasso.load(R.drawable.img_book_placeholder).into(imageView)
    }

    // cover size
    val width = context.resources.getDimensionPixelSize(R.dimen.book_item_width)
    val ratio = width.toFloat() / bookWidth
    val height = (bookHeight * ratio).toInt()

    val lp = imageView.layoutParams
    lp.height = height
    imageView.layoutParams = lp

    if (obj.isSold()) {
      soldOutTextView.visibility = View.VISIBLE
      priceLayout.visibility = View.GONE
    } else {
      soldOutTextView.visibility = View.GONE
      priceLayout.visibility = View.VISIBLE

      priceTextView.text = context.getString(R.string.money_unit, obj.price)

      val qualityArray = context.resources.getStringArray(R.array.book_status_array)
      statusTextView.text = qualityArray[obj.qualityIndex()]
    }
  }
}
