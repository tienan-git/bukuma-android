package jp.com.labit.bukuma.model

import jp.com.labit.bukuma.model.realm.User

/**
 * Created by zoonooz on 10/18/2016 AD.
 * Book transaction
 */
class Transaction {
  var id = 0
  var uniqueId = ""
  var status = ""
  var remark = ""
  var merchandise: Merchandise? = null
  var user: User? = null
  var seller: User? = null
  var userAddress: Address? = null
  var actionExpireAt = 0L
  var updatedAt = 0L
  var createdAt = 0L

  companion object {
    val STATUS_INITIAL = "initial"
    val STATUS_SELLER_PREPARE = "seller_prepare"
    val STATUS_SELLER_SHIPPED = "seller_shipped"
    val STATUS_ITEM_ARRIVED = "buyer_item_arrived"
    val STATUS_FINISHED = "finished"
    val STATUS_PENDING_STAFF = "pending_review"
    val STATUS_CANCELLED = "cancelled"
  }

  /**
   * Check if transaction is cancellable
   *
   * @param currentTime current time in millisecond
   * @return true if it can be cancelled, false otherwise
   */
  fun cancellable(currentTime: Long): Boolean {
    val time = currentTime - createdAt * 1000 > 10 * 86400000L // > 10 days
    val statusCondition = status == STATUS_SELLER_PREPARE || status == STATUS_INITIAL
    return time && statusCondition
  }

  /**
   * Check if transaction is after three days
   *
   * @param currentTime current time in millisecond
   * @return true if it can be cancelled, false otherwise
   */
  fun afterThreeDays(currentTime: Long): Boolean {
    val time = currentTime - createdAt * 1000 > 3 * 86400000L // > 3 days
    val statusCondition = status == STATUS_FINISHED || status == STATUS_INITIAL
    return time && statusCondition
  }
}
