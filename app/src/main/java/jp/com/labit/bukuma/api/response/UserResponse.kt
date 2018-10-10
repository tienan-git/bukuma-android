package jp.com.labit.bukuma.api.response

import jp.com.labit.bukuma.model.realm.User

/**
 * Created by zoonooz on 9/20/2016 AD.
 * Response model for user api
 */

class GetTimestampResponse : BaseResponse() {
  var time: Long = 0
}

class AuthenUserResponse : BaseResponse() {
  var userId = 0
  var accessToken: String? = null
}

class GetUserResponse : BaseResponse() {
  var user: User? = null
}

class GetNotificationResponse : BaseResponse() {
  var notificationSetting = Setting()

  class Setting {
    var bookLike = true
    var bookUpdate = true
    var bookSale = true
    var transactions = true
    var message = true
    var news = true
    var transactionEmail = true
    var newsEmail = true
  }
}