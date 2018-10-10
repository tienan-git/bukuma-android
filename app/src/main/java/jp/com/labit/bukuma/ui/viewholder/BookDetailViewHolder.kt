package jp.com.labit.bukuma.ui.viewholder

import android.support.v7.widget.CardView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.URLSpanCallback
import jp.com.labit.bukuma.extension.stripUnderline
import jp.com.labit.bukuma.extension.toHtmlSpanned
import jp.com.labit.bukuma.model.Book
import jp.com.labit.bukuma.model.Tag
import jp.com.labit.bukuma.ui.custom.LikeView
import rx.Observable

/**
 * Created by zoonooz on 10/17/2016 AD.
 * Book detail view holder
 */
class BookDetailViewHolder(parent: ViewGroup) :
    BaseViewHolder(parent, R.layout.viewholder_book_detail), URLSpanCallback {

  val titleTextView = itemView.findViewById(R.id.title_textview) as TextView
  val publisherTextView = itemView.findViewById(R.id.publisher_textview) as TextView
  val publishedTextView = itemView.findViewById(R.id.published_textview) as TextView
  val storeTextView = itemView.findViewById(R.id.store_textview) as TextView
  val tagsTextView = itemView.findViewById(R.id.tags_textview) as TextView
  val cardImageView = itemView.findViewById(R.id.book_cardview) as CardView
  val bookImageView = itemView.findViewById(R.id.book_imageview) as ImageView

  val likeView = itemView.findViewById(R.id.like_view) as LikeView
  val likeTextView = itemView.findViewById(R.id.like_textview) as TextView

  val lowestLayout = itemView.findViewById(R.id.lowest_layout) as ViewGroup
  val lowestTitleTextView = itemView.findViewById(R.id.lowest_title_textview) as TextView
  val lowestPriceTextView = itemView.findViewById(R.id.lowest_price_textview) as TextView
  val lowestStatusTextView = itemView.findViewById(R.id.lowest_status_textview) as TextView
  val lowestBuyButton = itemView.findViewById(R.id.lowest_buy_button) as Button

  val summaryLayout = itemView.findViewById(R.id.summary_layout) as ViewGroup
  val summaryTextView = itemView.findViewById(R.id.summary_textview) as TextView

  val tagLinkClick: Observable<String>
    get() = Observable.create { subscriber ->
      urlSpanCallback = object: URLSpanCallback {
        override fun onLinkClick(link: String) {
          if (!subscriber.isUnsubscribed) {
            subscriber.onNext(link)
          }
        }
      }
    }

  private var urlSpanCallback: URLSpanCallback? = null

  fun setObject(book: Book, tags: List<Tag>) {
    val context = itemView.context
    titleTextView.text = book.titleText()
    publisherTextView.text = book.publisherText()
    publishedTextView.text = context.getString(R.string.book_detail_published, book.publishedText())
    storeTextView.text = context.getString(R.string.book_detail_store_price, book.storePrice)

    if (tags.isEmpty()) {
      tagsTextView.visibility = View.GONE
    } else {
      tagsTextView.visibility = View.VISIBLE
      tagsTextView.text = createTagsText(tags)
      tagsTextView.stripUnderline(this)
    }

    likeTextView.text = "${book.cachedVotesTotal}"
    likeView.setLike(book.isLiked, false)

    // cover
    val lp = cardImageView.layoutParams
    val width = lp.width
    val ratio = width.toFloat() / book.width()
    val height = (book.height() * ratio).toInt()

    lp.height = height
    cardImageView.layoutParams = lp

    val cover = book.coverImageUrl
    if (cover != null) {
      picasso.load(cover).into(bookImageView)
    } else {
      picasso.load(R.drawable.img_book_placeholder).into(bookImageView)
    }

    // lowest merchandise
    lowestLayout.visibility = View.VISIBLE
    lowestTitleTextView.visibility = View.VISIBLE
    lowestBuyButton.visibility = View.VISIBLE
    if (book.isAvailable()) {
      val merchandise = book.lowestPriceMerchandise!!
      lowestPriceTextView.text = context.getString(R.string.money_unit, merchandise.price)
      val qualityArray = context.resources.getStringArray(R.array.book_status_array)
      lowestStatusTextView.text = context.getString(
          R.string.book_detail_status,
          qualityArray[merchandise.qualityIndex()])
    } else if (book.isSoldOut() || book.isNoSellerYet()) {
      lowestTitleTextView.visibility = View.GONE
      lowestBuyButton.visibility = View.GONE
      lowestPriceTextView.text = if (book.isSoldOut()) {
        context.getString(R.string.book_detail_sold_out)
      } else context.getString(R.string.book_detail_no_seller_yet)
      lowestStatusTextView.text = context.getString(R.string.book_detail_not_available_description)
    } else {
      lowestLayout.visibility = View.GONE
    }

    // summary
    if (book.summary != null && book.summary!!.isNotBlank()) {
      summaryTextView.text = book.summary
    } else {
      summaryLayout.visibility = View.GONE
    }
  }

  override fun onLinkClick(link: String) {
    urlSpanCallback?.onLinkClick(link)
  }

  private fun createTagsText(tags: List<Tag>): CharSequence {
    return tags.take(5)
        .map { "<a href=\"tag?id=${it.id}\">#${it.name}</a>" }
        .joinToString(" ")
        .toHtmlSpanned()
  }
}
