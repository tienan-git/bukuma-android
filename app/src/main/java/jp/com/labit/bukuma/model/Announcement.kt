package jp.com.labit.bukuma.model

/**
 * Created by zoonooz on 9/16/2016 AD.
 * Announcement model
 */
class Announcement {
  var id = 0
  var topic = ""
  var content = ""
  var url: String? = null
  var updatedAt: Long = 0
  var read = false
}