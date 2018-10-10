package jp.com.labit.bukuma.model

import jp.com.labit.bukuma.model.realm.User

/**
 * Created by zoonooz on 10/3/2016 AD.
 * Block model
 */
class BlockUser {
  var id = 0
  var createdAt: Long = 0
  var updatedAt: Long = 0
  var target: User? = null
}