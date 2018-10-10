package jp.com.labit.bukuma.api.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import rx.subjects.PublishSubject

/**
 * Created by tani on 2017/06/14.
 */

class ResponseInterceptor : Interceptor {

  val subject: PublishSubject<Response> = PublishSubject.create<Response>()

  override fun intercept(chain: Interceptor.Chain): Response {
    val response = chain.proceed(chain.request())
    subject.onNext(response)
    return response
  }
}