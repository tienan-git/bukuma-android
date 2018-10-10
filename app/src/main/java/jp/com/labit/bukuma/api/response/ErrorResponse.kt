package jp.com.labit.bukuma.api.response

/**
 * Created by zoonooz on 9/27/2016 AD.
 * Error response
 */
class ErrorResponse : BaseResponse() {
  var message: String? = null
  var errorCode: Int? = null
  var code: String? = null
}
