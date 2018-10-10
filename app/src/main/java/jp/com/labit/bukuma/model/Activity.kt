package jp.com.labit.bukuma.model

import com.google.gson.JsonObject
import jp.com.labit.bukuma.model.realm.User

/**
 * Created by zoonooz on 10/6/2016 AD.
 * Activity
 */
class Activity {
  var id = 0
  var key = ""
  var target: JsonObject? = null
  var user: User? = null
  var updatedAt: Long = 0
  var createdAt: Long = 0

  companion object {
    val KEY_BOOK_LIKE = "book.liked"
    val KEY_BOOK_PRICE = "book.selling_price"
    val KEY_BOOK_LIKE_PRICE = "book.liked_selling_price"
    val KEY_BOOK_UPDATE = "book.updated"
    val KEY_MERCHANDISE_BOUGHT = "merchandise.bought"

    /**
     * Array of currently support activities types
     */
    fun supportTypes() = arrayOf(
        KEY_BOOK_LIKE,
        KEY_BOOK_PRICE,
        KEY_BOOK_LIKE_PRICE,
        KEY_BOOK_UPDATE,
        KEY_MERCHANDISE_BOUGHT)
  }
}
