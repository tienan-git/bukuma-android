package jp.com.labit.bukuma.api.resource

import jp.com.labit.bukuma.BukumaConfig
import jp.com.labit.bukuma.api.BukumaService
import jp.com.labit.bukuma.api.response.BaseResponse
import jp.com.labit.bukuma.extension.realm
import jp.com.labit.bukuma.model.realm.ChatMerchandise
import jp.com.labit.bukuma.model.realm.ChatMessage
import jp.com.labit.bukuma.model.realm.ChatRoom
import jp.com.labit.bukuma.model.realm.ChatTransaction
import okhttp3.MediaType
import okhttp3.RequestBody
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.util.*

/**
 * Created by zoonooz on 10/6/2016 AD.
 * ChatRoom api
 */
class ChatRoomApi(var service: BukumaService, var config: BukumaConfig) {

  /**
   * Get specified chat room from id
   * also update locally
   *
   * @param roomId chat room id
   * @return [Single] of [ChatRoom] object
   */
  fun getChatRoom(roomId: Long): Single<ChatRoom> {
    return service.api.getChatroom(roomId)
        .map { it.convertToModel() }
        .map { presaveChatRoom(it, service.currentUser!!.id) }
        .doOnSuccess { res -> realm { it.copyToRealmOrUpdate(res) } }
  }

  /**
   * Get list of chat room from server
   * 1. get chat room list
   * 2. assign room id to sub-objects
   * 3. remove old member relation
   * 4. save to local
   *
   * @param fromTime only chat room updated after this time will be returned
   * @return [Single] of [ChatRoom] list
   */
  fun getChatRooms(fromTime: Long?): Single<List<ChatRoom>> {
    return service.api.getChatrooms(fromTime)
        .map { it.convertToModel().map { presaveChatRoom(it, service.currentUser!!.id) } }
        .doOnSuccess { res -> realm { it.copyToRealmOrUpdate(res) } }
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
  }

  /**
   * Create chat room
   * 1. find local first
   * 2. if local exist, check if it also exist in the server
   * 3. create chat room in server if need or using the old one if (2) exist
   * 4. create local room if newly create on server
   *
   * @param withUserId opponent user id
   * @return [Single] of [ChatRoom] that was created
   */
  fun createChatRoom(withUserId: Int): Single<ChatRoom> {
    var local: ChatRoom? = null
    realm {
      val result = it.where(ChatRoom::class.java)
          .equalTo("friendId", withUserId)
          .findFirst()
      result?.let { o ->
        local = it.copyFromRealm(o)
      }
    }

    val createObservable = service.api.createChatroom(withUserId)
        .flatMap { getChatRoom(it.roomId) }

    val resultObs = if (local != null) {
      getChatRoom(local!!.id).onErrorResumeNext(createObservable)
    } else {
      createObservable
    }

    return resultObs
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
  }

  /**
   * Get chat room message
   * 1. get chat message
   * 2. save to local
   *
   * @param roomId id of chat room
   * @param fromId from this message id to the latest
   * @return [Single] of [ChatMessage] list
   */
  fun getChatMessages(
      roomId: Long,
      fromId: Long? = null, toId: Long? = null): Single<List<ChatMessage>> {
    return service.api.getChatMessages(roomId, fromId, toId)
        .map { it.messages.map { presaveChatMessage(roomId, it.convertToModel()) } }
        .doOnSuccess { res -> realm { it.copyToRealmOrUpdate(res) } }
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
  }

  /**
   * Create chat message
   *
   * @param message [ChatMessage] object to create at server
   * @return [Single] of [ChatMessage] list that should contains just created message
   */
  fun createChatMessage(message: ChatMessage): Single<List<ChatMessage>> {

    val attachmentBody = message.attachment?.let {
      RequestBody.create(MediaType.parse("image/jpeg"), File(it))
    }

    // create temp message object
    val roomId = message.roomId
    // assign id if not exist. exist means resend
    val tempId = if (message.id == 0L) "-${Date().time / 100}".toLong() else message.id
    realm {
      message.id = tempId
      message.text = message.text?.trim()
      message.isSending = true
      message.createdAt = Date().time / 1000
      message.merchandise?.roomId = message.roomId
      message.itemTransaction?.roomId = message.roomId
      it.insertOrUpdate(message)
    }

    return service.api.createMessage(
        message.roomId,
        message.messageType,
        message.text,
        message.merchandise?.id,
        message.itemTransaction?.id,
        attachmentBody)

        // mark object error sent
        .doOnError {
          realm {
            val msg = it.where(ChatMessage::class.java).equalTo("id", tempId).findFirst()
            msg.isSending = false
            msg.isError = true
          }
        }
        // update message id to prevent refresh glitch
        .doOnSuccess { res ->
          realm {
            val msg = it.where(ChatMessage::class.java).equalTo("id", tempId).findFirst()
            msg.id = res.id
            msg.isSending = false
          }
        }
        // re-fetch the message immediately
        .flatMap { getChatMessages(roomId, it.id - 1, null) }
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
  }

  /**
   * Delete chat room
   *
   * 1. delete from server
   * 2. delete local data
   *
   * @param roomId chat room id that will be deleted
   * @return [Single] of [BaseResponse] indicate the result
   */
  fun deleteChatRoom(roomId: Long): Single<BaseResponse> {
    return service.api.deleteChatroom(roomId)
        .doOnSuccess {
          // remove room data
          realm {
            it.where(ChatMerchandise::class.java).equalTo("roomId", roomId).findAll().deleteAllFromRealm()
            it.where(ChatTransaction::class.java).equalTo("roomId", roomId).findAll().deleteAllFromRealm()
            it.where(ChatMessage::class.java).equalTo("roomId", roomId).findAll().deleteAllFromRealm()
            it.where(ChatRoom::class.java).equalTo("id", roomId).findAll().deleteAllFromRealm()
          }
        }
  }

  // -----------------------

  private fun presaveChatRoom(room: ChatRoom, userId: Int): ChatRoom {
    room.lastMessage?.let { presaveChatMessage(room.id, it) }
    room.userId = userId
    room.friendId = room.friend()?.id
    return room
  }

  private fun presaveChatMessage(roomId: Long, message: ChatMessage): ChatMessage {
    message.roomId = roomId
    message.isSent = true
    message.merchandise?.roomId = roomId
    message.itemTransaction?.roomId = roomId
    return message
  }
}
