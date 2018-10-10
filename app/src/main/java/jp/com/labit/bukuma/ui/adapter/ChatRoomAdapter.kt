package jp.com.labit.bukuma.ui.adapter

import android.support.v7.widget.RecyclerView
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.model.realm.ChatRoom
import jp.com.labit.bukuma.ui.viewholder.ChatRoomViewHolder
import rx.Observable
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 11/9/2016 AD.
 * Chat room list adapter
 */
class ChatRoomAdapter : BaseSimpleAdapter<ChatRoom, ChatRoomViewHolder>(ChatRoomViewHolder::class.java) {

  var userClick: Observable<ChatRoom> = PublishSubject.create()

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    super.onBindViewHolder(holder, position)

    val viewHolder = holder as ChatRoomViewHolder
    val room = getItem(position)

    RxView.clicks(viewHolder.avatarImageView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      (userClick as PublishSubject).onNext(room)
    }
  }
}
