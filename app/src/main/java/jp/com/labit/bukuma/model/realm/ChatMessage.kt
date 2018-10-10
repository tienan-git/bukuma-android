package jp.com.labit.bukuma.model.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by zoonooz on 10/6/2016 AD.
 * Message
 */
open class ChatMessage : RealmObject() {

  companion object {
    enum class Type(val intValue: Int, val stringValue: String) {
      TEXT(0, "text"),
      IMAGE(1, "image"),
      MERCHANDISE(2, "merchandise"),
      STATUS(3, "status_update"),

      // Local type
      STATUS_BOOK(4, "status_update_book"),
      STATUS_ICON(5, "status_update_icon"),
      STATUS_DESC(6, "status_update_desc"),
    }
  }

  @PrimaryKey var id = 0L
  var text: String? = null
  var messageType = ""
  var createdAt = 0L
  var attachment: String? = null
  var user: ChatMember = ChatMember()
  var merchandise: ChatMerchandise? = null
  var itemTransaction: ChatTransaction? = null

  // local only
  var roomId = 0L
  var isSent = false
  var isSending = false
  var isError = false

  /**
   * Return enum [Type] instead of string from server
   * @return [Type] enum
   */
  fun type(): Type {
    return when (messageType) {
      "image" -> Type.IMAGE
      "merchandise" -> Type.MERCHANDISE
      "status_update" -> {
        when (text) {
          "seller_shipped", "buyer_item_arrived" -> Type.STATUS_ICON
          "finished" -> Type.STATUS_DESC
          else -> Type.STATUS_BOOK
        }
      }
      else -> Type.TEXT
    }
  }
}
