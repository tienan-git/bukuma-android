package jp.com.labit.bukuma.api.resource

import jp.com.labit.bukuma.BukumaConfig
import jp.com.labit.bukuma.api.BukumaService
import jp.com.labit.bukuma.api.OmiseApi
import jp.com.labit.bukuma.api.response.BaseResponse
import okhttp3.Credentials
import rx.Single

/**
 * Created by zoonooz on 10/5/2016 AD.
 * Credit card api
 */
class CreditCardApi(var service: BukumaService, var config: BukumaConfig, var omise: OmiseApi) {

  /**
   * Create credit card
   * 1. get token from Omise
   * 2. create card at server
   *
   * @param name name that appear on the card
   * @param number card number
   * @param ccv card security code
   * @param expYear expiry year (2016, ...)
   * @param expMonth expiry month (1-12)
   * @return [Single] of [BaseResponse] indicating the result
   */
  fun createCreditCard(
      name: String, number: String, ccv: String,
      expYear: Int, expMonth: Int): Single<BaseResponse> {
    val authen = Credentials.basic(config.omiseKey, "x")
    val card = OmiseApi.Card(name, number, expMonth, expYear, ccv)
    return omise.createToken(authen, OmiseApi.TokenRequest(card))
        .flatMap {
          service.api.createCreditCard(1,
              it.card!!.id!!,
              it.id,
              expYear, expMonth,
              it.card!!.lastDigits!!,
              name)
        }
  }
}