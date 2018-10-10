package jp.com.labit.bukuma.ui.viewholder

import android.text.Spanned
import android.text.format.DateUtils
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.getAsObjectOrNull
import jp.com.labit.bukuma.extension.getAsPrimitiveOrNull
import jp.com.labit.bukuma.extension.toHtmlSpanned
import jp.com.labit.bukuma.model.Activity
import jp.com.labit.bukuma.ui.custom.PicassoCircleTransform

/**
 * Created by zoonooz on 10/6/2016 AD.
 * Activity view holder
 */
class ActivityViewHolder(parent: ViewGroup) : BaseObjectViewHolder<Activity>(parent, R.layout.viewholder_activity) {

  val avatarImageView = itemView.findViewById(R.id.avatar_imageview) as ImageView
  val textView = itemView.findViewById(R.id.textview) as TextView
  val timeTextView = itemView.findViewById(R.id.time_textview) as TextView
  val bookImageView = itemView.findViewById(R.id.book_imageview) as ImageView

  override fun setObject(obj: Activity) {
    val context = itemView.context
    // time
    timeTextView.text = DateUtils.getRelativeTimeSpanString((obj.createdAt * 1000),
        System.currentTimeMillis(),
        0L,
        DateUtils.FORMAT_ABBREV_ALL)

    // image
    val avatar = obj.user?.profileIcon
    if (avatar != null) {
      picasso.load(avatar).transform(PicassoCircleTransform()).fit().centerCrop().into(avatarImageView)
    } else {
      picasso.load(R.drawable.img_thumbnail_user).transform(PicassoCircleTransform()).into(avatarImageView)
    }

    var text: Spanned? = null
    var bookCover: String? = null
    var bookTitle = ""
    var bookWidth = 0
    var bookHeight = 0
    val username = obj.user?.nickname ?: ""

    when (obj.key) {
      Activity.KEY_BOOK_LIKE, Activity.KEY_BOOK_LIKE_PRICE, Activity.KEY_BOOK_PRICE, Activity.KEY_BOOK_UPDATE -> {
        bookCover = obj.target?.getAsPrimitiveOrNull("cover_image_url")?.asString
        bookTitle = obj.target?.getAsPrimitiveOrNull("title")?.asString ?: ""
        bookWidth = obj.target?.getAsPrimitiveOrNull("image_width")?.asInt ?: 0
        bookHeight = obj.target?.getAsPrimitiveOrNull("image_height")?.asInt ?: 0
      }
      Activity.KEY_MERCHANDISE_BOUGHT -> {
        val book = obj.target?.getAsObjectOrNull("book")
        bookCover = book?.getAsPrimitiveOrNull("cover_image_url")?.asString
        bookTitle = book?.getAsPrimitiveOrNull("title")?.asString ?: ""
        bookWidth = book?.getAsPrimitiveOrNull("image_width")?.asInt ?: 0
        bookHeight = book?.getAsPrimitiveOrNull("image_height")?.asInt ?: 0
      }
    }

    when (obj.key) {
      Activity.KEY_BOOK_LIKE -> {
        text = context.getString(R.string.activities_text_book_like, bookTitle).toHtmlSpanned()
      }
      Activity.KEY_BOOK_LIKE_PRICE, Activity.KEY_BOOK_PRICE, Activity.KEY_BOOK_UPDATE -> {
        text = context.getString(R.string.activities_text_book_price, bookTitle).toHtmlSpanned()
      }
      Activity.KEY_MERCHANDISE_BOUGHT -> {
        text = context.getString(R.string.activities_text_book_merchandise_bought, username, bookTitle).toHtmlSpanned()
      }
    }

    textView.text = text

    if (bookCover != null) {
      picasso.load(bookCover).into(bookImageView)
    } else {
      picasso.load(R.drawable.img_book_placeholder).into(bookImageView)
    }

    // cover size
    val width = context.resources.getDimensionPixelSize(R.dimen.book_item_width)
    val ratio = width.toFloat() / bookWidth
    val height = (bookHeight * ratio).toInt()

    val lp = bookImageView.layoutParams
    lp.height = height
    bookImageView.layoutParams = lp
  }
}
