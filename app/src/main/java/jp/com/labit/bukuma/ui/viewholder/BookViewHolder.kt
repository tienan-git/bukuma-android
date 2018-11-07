package jp.com.labit.bukuma.ui.viewholder

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.Book
import jp.com.labit.bukuma.ui.custom.LikeView

/**
 * Created by zoonooz on 10/13/2016 AD.
 * View holder for Book
 */
class BookViewHolder(val parent: ViewGroup) : BaseObjectViewHolder<Book>(parent, R.layout.viewholder_book) {

  val imageView = itemView.findViewById(R.id.imageview) as ImageView
  val soldImageView = itemView.findViewById(R.id.sold_imageview) as ImageView
  val ribbonImageView = itemView.findViewById(R.id.ribbon_imageview) as ImageView
  val titleTextView = itemView.findViewById(R.id.textview) as TextView
  val priceTextView = itemView.findViewById(R.id.price_textview) as TextView
  val discountTextView = itemView.findViewById(R.id.discount_textview) as TextView
  val likeView = itemView.findViewById(R.id.like_view) as LikeView
  val likeTextView = itemView.findViewById(R.id.like_textview) as TextView

  override fun setObject(obj: Book) {
    val context = itemView.context
    titleTextView.text = obj.titleText()

    ribbonImageView.visibility = if (obj.isSeries()) View.VISIBLE else View.GONE
    if (obj.isSeries()) discountTextView.visibility = View.INVISIBLE
    if (obj.isSoldOut()) {
      priceTextView.text = context.getString(R.string.book_timeline_sold_out)
      discountTextView.visibility = View.INVISIBLE
      soldImageView.visibility = View.VISIBLE
      ribbonImageView.visibility = View.GONE
    } else if (obj.isNoSellerYet()) {
      priceTextView.text = context.getString(R.string.book_timeline_no_seller_yet)
      discountTextView.visibility = View.INVISIBLE
      soldImageView.visibility = View.GONE
    } else {
      val price = obj.lowestPrice ?: 0
      val priceDouble = java.lang.Double.parseDouble(price.toString())
      val storePriceDouble = java.lang.Double.parseDouble(obj.storePrice.toString())
      val discount = ((1 - priceDouble / storePriceDouble) * 100).toInt()
      priceTextView.text = context.getString(R.string.money_unit, price)
      if(discount >= 30) {
        discountTextView.text = context.getString(R.string.discount, discount)
      } else {
        discountTextView.visibility = View.INVISIBLE
      }
      soldImageView.visibility = View.GONE
    }
    likeTextView.text = "${obj.cachedVotesTotal}"
    likeView.setLike(obj.isLiked, false)

    val bookImage = obj.coverImageUrl
    if (bookImage != null) {
      picasso.load(bookImage).into(imageView)
    } else {
      picasso.load(R.drawable.img_book_placeholder).fit().into(imageView)
    }

    // cover size
    val column = context.resources.getInteger(R.integer.book_column)
    val margin = context.resources.getDimensionPixelSize(R.dimen.book_padding) * 2
    val columnSize = (parent.width - ((column + 1) * margin)) / column
    val width = columnSize
    val ratio = width.toFloat() / 128/*obj.width()*/
    val height = (182/*obj.height()*/ * ratio).toInt()

    val lp = imageView.layoutParams
    lp.height = height
    imageView.layoutParams = lp
  }
}
