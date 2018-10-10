package jp.com.labit.bukuma.api.response

import jp.com.labit.bukuma.model.CreditCard

/**
 * Created by zoonooz on 10/5/2016 AD.
 * CreditCard response
 */

class GetCreditCardResponse : BaseResponse() {
  var creditCard: CreditCard? = null
}

class GetCreditCardsResponse : BaseResponse() {
  var userPaymentSources = emptyList<CreditCard>()
}