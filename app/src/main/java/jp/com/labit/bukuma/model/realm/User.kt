package jp.com.labit.bukuma.model.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import jp.com.labit.bukuma.model.Merchandise

/**
 * Created by zoonooz on 9/15/2016 AD.
 * user model
 */
open class User : RealmObject() {
  @PrimaryKey var id = 0
  var email = ""
  var fullName: String? = null
  var nickname: String = ""
  var gender: Int? = null
  var biography: String? = null
  var provider = "email"
  var verified = false
  var phoneNumber: String? = null
  var paymentGatewayId: Int? = null
  var reviewedCount = 0
  var moodPositive = 0
  var moodSoso = 0
  var moodNegative = 0
  var updatedAt: Long = 0
  var createdAt: Long = 0
  var profileIcon: String? = null
  var inviteCode: String? = null
  var point = 0
  var bonusPoint = 0
  var merchandisesCount = 0
  var isOfficial = false

  // authorize
  var accessToken: String? = null

  companion object {
    val GENDER_MALE = 3
    val GENDER_FEMALE = 2
    val GENDER_OTHER = 1
    val GENDER_NONE = -1
  }

  /**
   * Purchase with price and return the numbers of point used
   * in Pair <bonusPoint, point>
   * minimum pay amount is 100
   *
   * @param merchandise item to calculate
   * @return [Pair] of <bonusPoint, point> that going to be used
   */
  fun purchasePrice(merchandise: Merchandise): Pair<Int, Int> {
    var bonusUsed = 0
    var pointUsed = 0
    val minPay = 100
    val price = merchandise.price
    val availableBonusPoint = if (merchandise.isBrandNew) 0 else bonusPoint

    if (availableBonusPoint + point >= price) {
      // can pay all by point
      bonusUsed = if (availableBonusPoint > price) price else availableBonusPoint
      if (price - bonusUsed > 0) pointUsed = price - bonusUsed
    } else {
      // cannot pay all
      var subtractablePrice = if (price > minPay) price - minPay else 0
      if (availableBonusPoint > 0 && subtractablePrice > 0) {
        if (subtractablePrice >= availableBonusPoint) bonusUsed = availableBonusPoint
        else bonusUsed = subtractablePrice
      }

      val amountLeft = price - bonusUsed
      subtractablePrice = if (amountLeft > minPay) amountLeft - minPay else 0
      if (subtractablePrice > 0 && point > 0) {
        if (subtractablePrice >= point) pointUsed = point
        else pointUsed = subtractablePrice
      }
    }

    return Pair(bonusUsed, pointUsed)
  }
}
