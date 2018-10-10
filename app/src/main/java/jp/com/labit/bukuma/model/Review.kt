package jp.com.labit.bukuma.model

import jp.com.labit.bukuma.model.realm.User

/**
 * Created by zoonooz on 9/28/2016 AD.
 * User review model
 */
class Review {
  var id = 0
  var comment: String = ""
  var updatedAt: Long = 0
  var createdAt: Long = 0
  var mood = 1
  var author: User? = null

  companion object {
    val USER_MOOD_POSITIVE = 1
    val USER_MOOD_NEGATIVE = -1
    val USER_MOOD_SOSO = 0
  }
}
