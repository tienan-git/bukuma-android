package jp.com.labit.bukuma.model.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by zoonooz on 10/6/2016 AD.
 * Chat member
 */
open class ChatMember : RealmObject() {
  @PrimaryKey var id = 0
  var email = ""
  var fullName: String? = null
  var nickname = ""
  var gender: Int? = null
  var biography: String? = null
  var moodPositive = 0
  var moodSoso = 0
  var moodNegative = 0
  var reviewedCount = 0
  var merchandisesCount = 0
  var updatedAt: Long = 0
  var createdAt: Long = 0
  var profileIcon: String? = null
}
