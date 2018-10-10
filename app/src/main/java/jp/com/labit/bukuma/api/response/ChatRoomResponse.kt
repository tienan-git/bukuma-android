package jp.com.labit.bukuma.api.response

import jp.com.labit.bukuma.model.Merchandise
import jp.com.labit.bukuma.model.Transaction
import jp.com.labit.bukuma.model.realm.*

/**
 * Created by zoonooz on 10/6/2016 AD.
 * Chatroom response
 */

class GetChatRoomResponse : BaseResponse() {
  var chat: ChatRoomResponse? = null

  fun convertToModel(): ChatRoom {
    return chat!!.convertToModel()
  }
}

class GetChatRoomsResponse : BaseResponse() {
  var chatRooms = emptyList<ChatRoomResponse>()

  fun convertToModel(): List<ChatRoom> {
    return chatRooms.map { it.convertToModel() }
  }
}

class GetChatMessagesResponse : BaseResponse() {
  var messages = emptyList<ChatMessageResponse>()
}

class ChatRoomResponse {
  var id = 0L
  var updatedAt = 0L
  var unreadCount = 0
  var lastMessage: ChatMessageResponse? = null
  var members: List<ChatMemberResponse> = emptyList()

  fun convertToModel(): ChatRoom {
    val chatroom = ChatRoom()
    chatroom.id = id
    chatroom.updatedAt = updatedAt
    chatroom.unreadCount = unreadCount
    chatroom.lastMessage = lastMessage?.convertToModel()
    chatroom.members.addAll(members.map { it.convertToModel() })
    return chatroom
  }
}

class ChatMemberResponse {
  var member: ChatMember? = null

  fun convertToModel(): ChatMember {
    return member!!
  }
}

class ChatMessageResponse {
  var id = 0L
  var text: String? = null
  var messageType = ""
  var createdAt = 0L
  var attachment: String? = null
  var user: ChatMember? = null
  var merchandise: Merchandise? = null
  var itemTransaction: Transaction? = null

  fun convertToModel(): ChatMessage {
    val msg = ChatMessage()
    msg.id = id
    msg.text = text
    msg.messageType = messageType
    msg.createdAt = createdAt
    msg.attachment = attachment
    user?.let { msg.user = it }

    merchandise?.let {
      val mer = ChatMerchandise()
      mer.id = it.id
      mer.price = it.price
      mer.soldAt = it.soldAt
      mer.bookTitle = it.title() ?: ""

      it.book?.let {
        mer.bookId = it.id
        mer.bookCover = it.coverImageUrl
        mer.bookWidth = it.imageWidth
        mer.bookHeight = it.imageHeight
      }

      msg.merchandise = mer
    }

    itemTransaction?.let {
      val tra = ChatTransaction()
      tra.id = it.id
      tra.status = it.status

      it.merchandise?.let {
        tra.price = it.price
        tra.bookTitle = it.title() ?: ""
      }

      it.merchandise?.book?.let {
        tra.bookId = it.id
        tra.bookCover = it.coverImageUrl
        tra.bookWidth = it.imageWidth
        tra.bookHeight = it.imageHeight
      }

      msg.itemTransaction = tra
    }

    return msg
  }
}

class CreateChatRoomResponse : BaseResponse() {
  var roomId = 0L
}

class CreateChatMessageResponse : BaseResponse() {
  var id = 0L
}
