package jp.com.labit.bukuma.model


/**
 * Created by zoonooz on 11/14/2016 AD.
 * Point transaction
 */
class PointTransaction {
  var id = 0
  var status: String? = null
  var pointChanged = 0
  var bonusPointChanged = 0
  var newPoint = 0
  var newBonusPoint = 0
  var oldPoint = 0
  var oldBonusPoint = 0
  var description = ""
  var remainingPoint = 0
  var creditPoint = 0
  var createdAt = 0L
  var updatedAt = 0L
  var expiredAt = 0L

  var merchandise: Merchandise? = null
  var book: Book? = null

  fun pointType(): Int {
    if (pointChanged != 0) return POINT_TYPE_NORMAL
    else if (bonusPointChanged != 0) return POINT_TYPE_BONUS
    else return POINT_TYPE_CREDIT
  }

  fun valueType(): Int {
    if (pointChanged > 0 || bonusPointChanged > 0 || creditPoint > 0) return VALUE_TYPE_PLUS
    else if (pointChanged < 0 || bonusPointChanged < 0 || creditPoint < 0) return VALUE_TYPE_MINUS
    else return VALUE_TYPE_UNKNOWN
  }

  /**
   * return type index from transaction description to use locally.
   * Logic and string comes from iOS side
   * @return type index
   */
  fun type(): Int {
    // logic taken from iOS
    if (description.contains("signed up from")) return TYPE_SIGNIN
    else if (description.contains("signed up bonus")) return TYPE_FIRST
    else if (description.contains("Bought merchandise")) return TYPE_BUY_MERCHANDISE
    else if (description.contains("purchased from card")) return TYPE_BUY_POINT
    else if (description.contains("sold merchandise")) return TYPE_SOLD_MERCHANDISE
    else if (description.contains("Withdraw")) return TYPE_WITHDRAW
    else if (description.contains("Refunded")) {
      if (pointType() == POINT_TYPE_BONUS) return TYPE_REFUND_BONUS
      else return TYPE_REFUND_NORMAL
    } else if (description.contains("Admin")) {
      if (pointType() == POINT_TYPE_BONUS) return TYPE_ADMIN_BONUS
      else return TYPE_ADMIN_NORMAL
    }
    else if (description.contains("campaign") || description.contains("Campaign")) return TYPE_CAMPAIGN
    else if (description.contains("expired")) return TYPE_EXPIRE
    else if (description.contains("Bought from card")) return TYPE_BUY_FROM_CARD_LOCAL
    else if (description.contains("credit point")) return TYPE_BUY_FROM_CARD
    else return TYPE_UNKNOWN
  }

  companion object {
    val TYPE_FIRST = 0
    val TYPE_SIGNIN = 1
    val TYPE_BUY_MERCHANDISE = 2
    val TYPE_BUY_POINT = 3
    val TYPE_SOLD_MERCHANDISE = 4
    val TYPE_WITHDRAW = 5
    val TYPE_REFUND_BONUS = 6
    val TYPE_REFUND_NORMAL = 7
    val TYPE_ADMIN_BONUS = 8
    val TYPE_ADMIN_NORMAL = 9
    val TYPE_CAMPAIGN = 10
    val TYPE_EXPIRE = 11
    val TYPE_UNKNOWN = -1
    val TYPE_BUY_FROM_CARD_LOCAL = 12
    val TYPE_BUY_FROM_CARD = 13

    val POINT_TYPE_BONUS = 0
    val POINT_TYPE_NORMAL = 1
    val POINT_TYPE_CREDIT = 2

    val VALUE_TYPE_PLUS = 0
    val VALUE_TYPE_MINUS = 1
    val VALUE_TYPE_UNKNOWN = 2
  }
}