package jp.com.labit.bukuma.api

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import rx.Single

/**
 * Created by zoonooz on 10/5/2016 AD.
 * Omise
 */
interface OmiseApi {

  @POST("tokens")
  fun createToken(
      @Header("Authorization") authen: String,
      @Body request: TokenRequest): Single<TokenResponse>

  data class Card (
      val name: String,
      val number: String,
      val expirationMonth: Int,
      val expirationYear: Int,
      val securityCode: String,
      val id: String? = null,
      val lastDigits: String? = null)

  data class TokenResponse(var id: String, var card: Card? = null)
  data class TokenRequest(val card: Card)
}