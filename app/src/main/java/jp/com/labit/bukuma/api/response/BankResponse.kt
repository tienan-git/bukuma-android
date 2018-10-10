package jp.com.labit.bukuma.api.response

import jp.com.labit.bukuma.model.Bank

/**
 * Created by zoonooz on 9/30/2016 AD.
 * Response model for Bank
 */
class GetBanksResponse : BaseResponse() {
  var bank_accounts = emptyList<Bank>()
}