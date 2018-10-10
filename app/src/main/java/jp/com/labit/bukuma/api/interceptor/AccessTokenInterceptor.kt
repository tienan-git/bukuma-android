package jp.com.labit.bukuma.api.interceptor

import io.realm.Realm
import jp.com.labit.bukuma.model.realm.User
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

/**
 * Created by zoonooz on 9/21/2016 AD.
 * Interceptor to add token if user exist
 */
class AccessTokenInterceptor : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    var request = chain.request()
    val requestBuilder = request.newBuilder()

    // get first user from database
    val realm = Realm.getDefaultInstance()
    var user = realm.where(User::class.java).findFirst()
    user?.let { user = realm.copyFromRealm(it) }
    realm.close()

    // if user exist, add token
    user?.let {
      val token = "Bearer " + it.accessToken
      requestBuilder.header("Authorization", token)
      Timber.d("added token : $token")
    }

    // build new request and proceed the chain
    request = requestBuilder.build()
    return chain.proceed(request)
  }
}
