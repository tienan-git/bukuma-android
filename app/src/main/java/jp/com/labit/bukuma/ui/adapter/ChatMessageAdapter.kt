package jp.com.labit.bukuma.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import io.realm.RealmResults
import jp.com.labit.bukuma.model.realm.ChatMessage
import jp.com.labit.bukuma.ui.viewholder.ChatMessageViewHolder
import rx.Observable
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 10/11/2016 AD.
 * Chat message adapter
 */
class ChatMessageAdapter(var currentUserId: Int, messageResult: RealmResults<ChatMessage>) : BaseAdapter<ChatMessage>() {

  var itemClick: Observable<ChatMessage> = PublishSubject.create()

  init {
    items = messageResult
  }

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
    super.onAttachedToRecyclerView(recyclerView)
    (items as RealmResults).addChangeListener {
      notifyDataSetChanged()
    }
  }

  override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
    (items as RealmResults).removeChangeListeners()
    super.onDetachedFromRecyclerView(recyclerView)
  }

  override fun getItemViewType(position: Int): Int {
    val message = getItem(position)
    val side = if (message.type() == ChatMessage.Companion.Type.STATUS_DESC) {
      ChatMessageViewHolder.TYPE_FULL
    } else if (currentUserId == getItem(position).user.id) {
      ChatMessageViewHolder.TYPE_RIGHT
    } else {
      ChatMessageViewHolder.TYPE_LEFT
    }

    return message.type().intValue or side
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return ChatMessageViewHolder(parent, viewType, currentUserId)
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val viewHolder = holder as ChatMessageViewHolder
    val msg = getItem(position)
    val prev = if (position + 1 < itemCount) getItem(position + 1) else null
    viewHolder.setMessage(msg, prev)

    RxView.clicks(viewHolder.contentLayout).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      (itemClick as PublishSubject).onNext(msg)
    }
  }
}
