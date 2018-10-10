package jp.com.labit.bukuma.api.response

import jp.com.labit.bukuma.model.Activity

/**
 * Created by zoonooz on 10/6/2016 AD.
 * Activities response
 */

class GetActivityResponse : BaseResponse() {
  var activities = emptyList<Activity>()
}

class GetActivityCountResponse : BaseResponse() {
  var count = 0
}