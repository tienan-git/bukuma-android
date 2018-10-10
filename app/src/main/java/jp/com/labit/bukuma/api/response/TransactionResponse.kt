package jp.com.labit.bukuma.api.response

import jp.com.labit.bukuma.model.Transaction

/**
 * Created by zoonooz on 10/18/2016 AD.
 * Transaction responses
 */

class GetTransactionsResponse : BaseResponse() {
  var itemTransactions = emptyList<Transaction>()
}

class GetTransactionResponse : BaseResponse() {
  var itemTransaction: Transaction? = null
}

class GetTransactionCountResponse : BaseResponse() {
  var count = 0
}