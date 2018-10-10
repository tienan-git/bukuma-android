package jp.com.labit.bukuma.model

import java.util.*

/**
 * Created by zoonooz on 10/5/2016 AD.
 * Credit Card
 */
class CreditCard {
  var id = 0
  var last_4 = ""
  var name = ""
  var expMonth = 0
  var expYear = 0
  var active = false
  var default = false
  var userAddressId = 0
  var userPaymentGatewayId = 0
  var externalId = ""
  var updatedAt: Long = 0
  var info: Info? = null

  class Info {
    var brand = ""
    var securityCodeCheck = false
  }

  fun isExpired(): Boolean {
    val calender = Calendar.getInstance()
    val year = calender.get(Calendar.YEAR)
    val month = calender.get(Calendar.MONTH) + 1
    if (expYear != year) {
      return expYear < year
    }
    return expMonth < month
  }
}