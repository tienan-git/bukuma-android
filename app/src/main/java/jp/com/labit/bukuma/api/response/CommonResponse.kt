package jp.com.labit.bukuma.api.response

import jp.com.labit.bukuma.model.realm.Banner

/**
 * Created by zoonooz on 9/9/2016 AD.
 * Base response from api
 */
open class BaseResponse {
  var result = "unknown"
}

// app config
class AppConfigResponse : BaseResponse() {

  var app: App? = null

  class App {
    var id = 0
    var name = ""
    var settings: Settings? = null
  }

  class Settings {
    var latestAndroidVersion = "0"
    var isOnBati = "0"
    var isMaintenanced = "0"
    var isServiceStopped = "0"
    var ngWords = ""
    var maintenanceUrl = ""
    var initialPoint = "300"
    var invitationPoint = "300"
    var contactFormMessage = ""
    var transferFee = ""
    var needTrasferFee = ""
    var minApplicableAmount = ""
    var maxApplicableAmount = ""
    var salesCommissionDate = ""
    var salesCommissionPercent = ""
    var minPrice = ""
    var maxPrice = ""
  }
}

// app banners
class AppBannersResponse : BaseResponse() {
  var banners = emptyList<Banner>()
}