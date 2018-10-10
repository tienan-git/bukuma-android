package jp.com.labit.bukuma.ui.viewholder

import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.Transaction
import jp.com.labit.bukuma.model.realm.ChatRoom
import jp.com.labit.bukuma.ui.custom.PicassoCircleTransform

/**
 * Created by zoonooz on 10/6/2016 AD.
 * Chatroom view holder
 */
class ChatRoomViewHolder(parent: ViewGroup) : BaseObjectViewHolder<ChatRoom>(parent, R.layout.viewholder_chatroom) {

  val avatarImageView = itemView.findViewById(R.id.avatar_imageview) as ImageView
  val textView = itemView.findViewById(R.id.name_textview) as TextView
  val messageTextView = itemView.findViewById(R.id.message_textview) as TextView
  val timeTextView = itemView.findViewById(R.id.time_textview) as TextView
  val unreadTextView = itemView.findViewById(R.id.unread_textview) as TextView

  override fun setObject(obj: ChatRoom) {
    val context = itemView.context
    val friend = obj.friend()

    // unread
    if (obj.unreadCount > 0) {
      unreadTextView.visibility = View.VISIBLE
      unreadTextView.text = if (obj.unreadCount > 99) "99+" else "${obj.unreadCount}"
    } else {
      unreadTextView.visibility = View.GONE
    }

    // time
    timeTextView.text = DateUtils.getRelativeTimeSpanString((obj.updatedAt * 1000),
        System.currentTimeMillis(),
        0L,
        DateUtils.FORMAT_ABBREV_ALL)

    // image
    val avatar = friend?.profileIcon
    if (avatar != null) {
      picasso.load(avatar).transform(PicassoCircleTransform()).fit().centerCrop().into(avatarImageView)
    } else {
      picasso.load(R.drawable.img_thumbnail_user).transform(PicassoCircleTransform()).into(avatarImageView)
    }

    textView.text = friend?.nickname

    // last message text
    messageTextView.text = obj.lastMessage?.text
    // image
    obj.lastMessage?.attachment?.let {
      messageTextView.text = context.getString(R.string.chatroom_message_image)
    }
    // transaction
    obj.lastMessage?.itemTransaction?.let {
      messageTextView.text = when (it.status) {
        Transaction.STATUS_SELLER_PREPARE -> context.getString(R.string.chatroom_message_prepare)
        Transaction.STATUS_SELLER_SHIPPED -> context.getString(R.string.chatroom_message_shipped)
        Transaction.STATUS_ITEM_ARRIVED -> context.getString(R.string.chatroom_message_arrived)
        Transaction.STATUS_FINISHED -> context.getString(R.string.chatroom_message_finished)
        else -> ""
      }
    }
    // user
    if (obj.friend() == null) {
      messageTextView.text = context.getString(R.string.chatroom_message_closed)
    }
  }
}
