package jp.com.labit.bukuma.api.response

import jp.com.labit.bukuma.model.PointTransaction

/**
 * Created by zoonooz on 11/14/2016 AD.
 * Point transaction response model
 */

class GetPointTransactionsResponse : BaseResponse() {
  var pointTransactions = emptyList<PointTransaction>()
  var userPoint = 0
  var userBonusPoint = 0
  var nearExpireBonusPoint = 0
  var nearExpireBonusPointDatetime = 0L
}

class GetExpiringPointResponse : BaseResponse() {
  var point = 0
  var bonusPoint = 0
  var normalPoint = 0
}