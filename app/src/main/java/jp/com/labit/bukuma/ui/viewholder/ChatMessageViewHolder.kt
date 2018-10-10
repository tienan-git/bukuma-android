package jp.com.labit.bukuma.ui.viewholder

import android.content.Context
import android.graphics.Color
import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.toHtmlSpanned
import jp.com.labit.bukuma.model.Transaction
import jp.com.labit.bukuma.model.realm.ChatMessage
import jp.com.labit.bukuma.model.realm.ChatTransaction
import jp.com.labit.bukuma.ui.custom.PicassoCircleTransform
import jp.com.labit.bukuma.ui.custom.PicassoRoundTransform
import jp.com.labit.bukuma.ui.custom.TriangleView
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by zoonooz on 10/11/2016 AD.
 * Chat message view holder
 */
class ChatMessageViewHolder(parent: ViewGroup, var type: Int, var userId: Int, var text: String? = null) :
    BaseObjectViewHolder<ChatMessage>(
        parent,
        if (type and TYPE_FULL == TYPE_FULL)
          R.layout.chat_full
        else if (type and TYPE_LEFT == TYPE_LEFT)
          R.layout.chat_left
        else R.layout.chat_right) {

  companion object {
    val TYPE_LEFT = 0b100000
    val TYPE_RIGHT = 0b010000
    val TYPE_FULL = 0b110000
  }

  val contentLayout = itemView.findViewById(R.id.content_layout) as ViewGroup
  val avatarImageView = itemView.findViewById(R.id.avatar_imageview) as? ImageView
  val statusImageView = itemView.findViewById(R.id.status_imageview) as? ImageView
  val timeTextView = itemView.findViewById(R.id.time_textview) as? TextView
  val dateTextView = itemView.findViewById(R.id.date_textview) as TextView
  val triangleView = itemView.findViewById(R.id.triangle) as? TriangleView

  val dateFormat = SimpleDateFormat("kk:mm", Locale.getDefault())

  init {
    val stub = itemView.findViewById(R.id.stub) as ViewStub
    when (type and 0b001111) { // clear side bits
      ChatMessage.Companion.Type.IMAGE.intValue -> stub.layoutResource = R.layout.chat_image
      ChatMessage.Companion.Type.MERCHANDISE.intValue -> stub.layoutResource = R.layout.chat_merchandise
      ChatMessage.Companion.Type.STATUS_DESC.intValue -> stub.layoutResource = R.layout.chat_transaction_desc
      ChatMessage.Companion.Type.STATUS_ICON.intValue -> stub.layoutResource = R.layout.chat_transaction_icon
      ChatMessage.Companion.Type.STATUS_BOOK.intValue -> stub.layoutResource = R.layout.chat_transaction_book
      else -> stub.layoutResource = R.layout.chat_text
    }
    stub.inflate()
  }

  override fun setObject(obj: ChatMessage) {
    Timber.i("holder message type ${obj.messageType}")
    val msgType = obj.type()
    // set common values
    val date = Date(obj.createdAt * 1000)
    timeTextView?.text = dateFormat.format(date)

    // sending status
    statusImageView?.let {
      if (obj.isError) {
        it.setImageResource(R.drawable.ic_chat_sending_alert)
        it.visibility = View.VISIBLE
      } else if (obj.isSending) {
        it.setImageResource(R.drawable.ic_chat_sending_arrow)
        it.visibility = View.VISIBLE
      } else {
        it.visibility = View.GONE
      }
    }

    // reset views
    triangleView?.visibility = View.VISIBLE

    // specific type values
    val stubView = itemView.findViewById(R.id.stub_view)
    when (msgType) {
      ChatMessage.Companion.Type.IMAGE -> setImage(stubView, obj)
      ChatMessage.Companion.Type.MERCHANDISE -> setMerchandise(stubView, obj)
      ChatMessage.Companion.Type.STATUS_ICON -> setTransactionIcon(stubView, obj)
      ChatMessage.Companion.Type.STATUS_DESC -> setTransactionDesc(stubView, obj.itemTransaction)
      ChatMessage.Companion.Type.STATUS_BOOK -> setTransactionBook(stubView, obj.itemTransaction)
      else -> setText(stubView, obj)
    }
  }

  fun setMessage(message: ChatMessage, prev: ChatMessage?) {
    setObject(message)

    // image
    avatarImageView?.let {
      if (prev == null || prev.user.id != message.user.id) {
        it.visibility = View.VISIBLE
        val icon = message.user.profileIcon
        if (icon != null) {
          picasso.load(icon).fit().centerCrop().transform(PicassoCircleTransform()).into(it)
        } else {
          picasso.load(R.drawable.img_thumbnail_user).transform(PicassoCircleTransform()).into(it)
        }
      } else {
        it.visibility = View.INVISIBLE
      }
    }

    // show date
    val showDate: Boolean
    if (prev == null) {
      showDate = true
    } else {
      val calendar = Calendar.getInstance()
      calendar.timeInMillis = message.createdAt * 1000
      val currentDate = calendar.get(Calendar.DATE)
      calendar.timeInMillis = prev.createdAt * 1000
      val previousDate = calendar.get(Calendar.DATE)
      showDate = currentDate != previousDate
    }
    if (showDate) {
      dateTextView.text = DateUtils.getRelativeTimeSpanString(message.createdAt * 1000,
          System.currentTimeMillis(),
          DateUtils.DAY_IN_MILLIS,
          DateUtils.FORMAT_ABBREV_RELATIVE)
    }
    dateTextView.visibility = if (showDate) View.VISIBLE else View.GONE
  }

  // types

  private fun setText(view: View, message: ChatMessage) {
    val textView = view.findViewById(R.id.textview) as TextView
    textView.text = message.text
    textView.setTextColor(if (type and TYPE_RIGHT == TYPE_RIGHT) Color.WHITE else Color.BLACK)
  }

  private fun setImage(view: View, message: ChatMessage) {
    val context = itemView.context
    val imageView = view.findViewById(R.id.imageview) as ImageView
    message.attachment?.let {
      val radius = context.resources.getDimensionPixelSize(R.dimen.button_radius)
      // use file scheme if id < 0 (temp)
      picasso
          .load("${if (message.id < 0) "file:" else ""}$it")
          .fit()
          .centerCrop()
          .transform(PicassoRoundTransform(radius))
          .into(imageView)
    }

    triangleView?.visibility = View.INVISIBLE
  }

  private fun setMerchandise(view: View, message: ChatMessage) {
    val imageView = view.findViewById(R.id.book_imageview) as ImageView
    val titleTextView = view.findViewById(R.id.book_title_textview) as TextView
    val priceTextView = view.findViewById(R.id.book_price_textview) as TextView
    val context = view.context

    setText(view, message)

    message.merchandise?.let {
      titleTextView.text = context.getString(R.string.chat_message_merchandise_title, it.bookTitle).toHtmlSpanned()
      priceTextView.text = context.getString(R.string.money_unit, it.price)
    }

    val bookCover = message.merchandise?.bookCover
    val bookWidth = message.merchandise?.bookWidth ?: 0
    val bookHeight = message.merchandise?.bookHeight ?: 0
    setBookCover(context, bookCover, bookWidth, bookHeight, imageView)
  }

  private fun setTransactionDesc(view: View, transaction: ChatTransaction?) {
    val context = view.context
    val imageView = view.findViewById(R.id.book_imageview) as ImageView
    val descTextView = view.findViewById(R.id.desc_textview) as TextView

    val bookCover = transaction?.bookCover
    val bookWidth = transaction?.bookWidth ?: 0
    val bookHeight = transaction?.bookHeight ?: 0
    setBookCover(context, bookCover, bookWidth, bookHeight, imageView)

    descTextView.text = transaction?.bookTitle?.let {
      context.getString(R.string.chat_message_transaction_desc, it).toHtmlSpanned()
    } ?: ""
  }

  private fun setTransactionIcon(view: View, message: ChatMessage) {
    val context = view.context
    val imageView = view.findViewById(R.id.status_imageview) as ImageView
    val textView = view.findViewById(R.id.status_textview) as TextView

    textView.setTextColor(if (type and TYPE_RIGHT == TYPE_RIGHT) Color.WHITE else Color.BLACK)

    when (message.text) {
      Transaction.STATUS_SELLER_SHIPPED -> {
        imageView.setImageResource(R.drawable.img_chat_shipped)
        textView.text = context.getString(R.string.chat_message_transaction_shipped)
      }
      Transaction.STATUS_ITEM_ARRIVED -> {
        imageView.setImageResource(R.drawable.img_chat_tasks)
        textView.text = if (type and TYPE_RIGHT == TYPE_RIGHT) {
          context.getString(R.string.chat_message_transaction_arrived_buyer)
        } else {
          context.getString(R.string.chat_message_transaction_arrived_seller)
        }
      }
    }
  }

  private fun setTransactionBook(view: View, transaction: ChatTransaction?) {
    val context = view.context
    val imageView = view.findViewById(R.id.book_imageview) as ImageView
    val titleTextView = view.findViewById(R.id.book_title_textview) as TextView
    val priceTextView = view.findViewById(R.id.book_price_textview) as TextView
    val statusTextView = view.findViewById(R.id.status_textview) as TextView

    statusTextView.setTextColor(if (type and TYPE_RIGHT == TYPE_RIGHT) Color.WHITE else Color.BLACK)

    titleTextView.text = transaction?.bookTitle?.let {
      context.getString(
          R.string.chat_message_merchandise_title,
          transaction.bookTitle).toHtmlSpanned()
    } ?: ""

    priceTextView.text = transaction?.price?.let {
      context.getString(R.string.money_unit, it)
    } ?: ""

    val bookCover = transaction?.bookCover
    val bookWidth = transaction?.bookWidth ?: 0
    val bookHeight = transaction?.bookHeight ?: 0
    setBookCover(context, bookCover, bookWidth, bookHeight, imageView)
  }

  // util

  private fun setBookCover(
      context: Context,
      coverUrl: String?,
      w: Int, h: Int,
      imageView: ImageView) {

    imageView.setImageBitmap(null)
    if (coverUrl != null) {
      picasso.load(coverUrl).into(imageView)
    } else {
      picasso.load(R.drawable.img_book_placeholder).into(imageView)
    }

    // cover size
    val width = context.resources.getDimensionPixelSize(R.dimen.book_item_width)
    val ratio = width.toFloat() / w
    val height = (h * ratio).toInt()

    val lp = imageView.layoutParams
    lp.height = height
    imageView.layoutParams = lp
  }
}
