package jp.com.labit.bukuma.api.response

import jp.com.labit.bukuma.model.Review

/**
 * Created by zoonooz on 11/7/2016 AD.
 * Review response
 */

class GetReviewsResponse : BaseResponse() {
  val reviews = emptyList<Review>()
}