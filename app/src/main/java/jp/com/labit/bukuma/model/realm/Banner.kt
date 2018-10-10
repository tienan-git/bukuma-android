package jp.com.labit.bukuma.model.realm

import io.realm.RealmObject

/**
 * Created by zoonooz on 11/10/2016 AD.
 * Advertise banner
 */
open class Banner : RealmObject() {
  var name = ""
  var url = ""
  var appUrlScheme = ""
  var adsTitle = ""
  var adsDescription = ""
  var language = "ja"
  var isIncentive = false
  var position = ""
  var gender = "both"
  var place = ""
  var stage = 0
  var bannerUrl = ""
  var bannerIconUrl: String? = null
  var adsImage: String? = null
}