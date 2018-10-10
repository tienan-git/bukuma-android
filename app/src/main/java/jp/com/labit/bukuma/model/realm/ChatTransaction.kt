package jp.com.labit.bukuma.model.realm

import io.realm.RealmObject

/**
 * Created by zoonooz on 10/26/2016 AD.
 * Compact transaction for chat message
 */
open class ChatTransaction : RealmObject() {
  var id = 0
  var roomId = 0L
  var status = ""
  var price = 0
  var bookId = 0
  var bookTitle = ""
  var bookCover: String? = null
  var bookWidth = 0
  var bookHeight = 0
}
