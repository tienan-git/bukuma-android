package jp.com.labit.bukuma.api.response

import jp.com.labit.bukuma.model.Address

/**
 * Created by zoonooz on 10/3/2016 AD.
 * User address response
 */

class GetAddressResponse : BaseResponse() {
  var address: Address? = null
}

class GetAddressesResponse : BaseResponse() {
  var userAddresses = emptyList<Address>()
}

class GetPostalInfoResponse : BaseResponse() {
  var postalCode: PostalInfo? = null

  class PostalInfo {
    var id = 0
    var code = ""
    var prefecture = ""
    var city = ""
    var area = ""
    var otherInfo: String? = null
  }
}
