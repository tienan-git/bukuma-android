package jp.com.labit.bukuma.ui.viewholder

import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.toHtmlSpanned
import jp.com.labit.bukuma.model.Transaction
import jp.com.labit.bukuma.ui.custom.PicassoCircleTransform

/**
 * Created by zoonooz on 10/24/2016 AD.
 * Transaction view holder
 */
class TodoViewHolder(parent: ViewGroup, val userId: Int) : BaseObjectViewHolder<Transaction>(parent, R.layout.viewholder_todo) {

  val avatarImageView = itemView.findViewById(R.id.avatar_imageview) as ImageView
  val todoTextView = itemView.findViewById(R.id.todo_textview) as TextView
  val textView = itemView.findViewById(R.id.textview) as TextView
  val timeTextView = itemView.findViewById(R.id.time_textview) as TextView
  val bookImageView = itemView.findViewById(R.id.book_imageview) as ImageView
  val colorBar = itemView.findViewById(R.id.color_bar) as TextView

  override fun setObject(obj: Transaction) {
    val context = itemView.context
    // time
    timeTextView.text = DateUtils.getRelativeTimeSpanString((obj.createdAt * 1000),
        System.currentTimeMillis(),
        0L,
        DateUtils.FORMAT_ABBREV_ALL)
    todoTextView.visibility = View.VISIBLE

    val bookCover = obj.merchandise?.book?.coverImageUrl
    val bookWidth = obj.merchandise?.book?.imageWidth ?: 0
    val bookHeight = obj.merchandise?.book?.imageHeight ?: 0

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

    // text & image
    val isBuyer = obj.user?.id ?: 0 == userId
    val bookTitle = obj.merchandise?.title() ?: ""
    val username = obj.user?.nickname ?: ""
    val sellerName = obj?.seller?.nickname ?: ""

    val avatar = if (isBuyer) obj.seller?.profileIcon else obj.user?.profileIcon
    if (avatar != null) {
      picasso.load(avatar).transform(PicassoCircleTransform()).fit().centerCrop().into(avatarImageView)
    } else {
      picasso.load(R.drawable.img_thumbnail_user).transform(PicassoCircleTransform()).into(avatarImageView)
    }

    colorBar.setBackgroundResource(R.color.wating_item_bar)

    when (obj.status) {
      Transaction.STATUS_INITIAL -> {
        textView.text = context.getString(R.string.todo_text_seller_initial)
        todoTextView.visibility = View.GONE
      }
      Transaction.STATUS_SELLER_PREPARE -> {
        textView.text = if (isBuyer) {
          context.getString(R.string.todo_text_buyer_seller_prepare, sellerName).toHtmlSpanned()
        } else {
          context.getString(R.string.todo_text_seller_seller_prepare, username, bookTitle).toHtmlSpanned()
        }
        if(isBuyer) {
          todoTextView.visibility = View.GONE
        } else {
          todoTextView.text = context.getString(R.string.todo_text_seller_seller_prepare_title)
          colorBar.setBackgroundResource(R.color.todo_item_bar)
        }
      }
      Transaction.STATUS_SELLER_SHIPPED -> {
        textView.text = if (isBuyer) {
          context.getString(R.string.todo_text_buyer_shipped, sellerName).toHtmlSpanned()
        } else {
          context.getString(R.string.todo_text_seller_shipped, username).toHtmlSpanned()
        }
        if(isBuyer) {
          todoTextView.text = context.getString(R.string.todo_text_buyer_shipped_title)
          colorBar.setBackgroundResource(R.color.todo_item_bar)
        } else {
          todoTextView.visibility = View.GONE
        }
      }
      Transaction.STATUS_ITEM_ARRIVED -> {
        textView.text = if (isBuyer) {
          context.getString(R.string.todo_text_buyer_arrived, sellerName).toHtmlSpanned()
        } else {
          context.getString(R.string.todo_text_seller_arrived, username).toHtmlSpanned()
        }
        if(isBuyer) {
          todoTextView.visibility = View.GONE
        } else {
          todoTextView.text = context.getString(R.string.todo_text_seller_arrived_title)
          colorBar.setBackgroundResource(R.color.todo_item_bar)
        }
      }
      Transaction.STATUS_FINISHED -> {
        textView.text = if (isBuyer) {
          context.getString(R.string.todo_text_buyer_finished)
        } else {
          context.getString(R.string.todo_text_seller_finished)
        }
        todoTextView.visibility = View.GONE
      }
      Transaction.STATUS_PENDING_STAFF, Transaction.STATUS_CANCELLED -> {
        textView.text = context.getString(R.string.todo_text_staff_pending)
        todoTextView.visibility = View.GONE
      }
      else -> textView.text = "Unknown"
    }
  }
}