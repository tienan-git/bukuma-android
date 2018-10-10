package jp.com.labit.bukuma.model.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by zoonooz on 10/6/2016 AD.
 * Chatroom model
 */
open class ChatRoom : RealmObject() {
  @PrimaryKey var id = 0L
  var updatedAt: Long = 0
  var unreadCount = 0
  var lastMessage: ChatMessage? = null
  var members: RealmList<ChatMember> = RealmList()

  // local only
  var userId = 0
  var friendId: Int? = null

  /**
   * find self user object in the room
   * @return [ChatMember] self user in the room
   */
  fun user(): ChatMember = members.first { it.id == userId }

  /**
   * find opponent user in the room
   * @return [ChatMember] opponent user
   */
  fun friend() = members.firstOrNull { it.id != userId }
}
