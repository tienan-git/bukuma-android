package jp.com.labit.bukuma.extension

import jp.com.labit.bukuma.ui.activity.BaseActivity
import jp.com.labit.bukuma.ui.dialog.LoginDialog
import rx.Observable

/**
 * Created by zoonooz on 10/4/2016 AD.
 * Observable extension
 */

/**
 * Convert observable of anything to observable of response so when error happened it will not
 * flow into [rx.Subscriber.onError]
 *
 * @return [Observable] of [Response]
 */
@Suppress("USELESS_CAST")
fun <T> Observable<T>.toResponse(): Observable<Response<T>> {
  return this
      .map { Response.Success(it) as Response<T> }
      .onErrorReturn { Response.Error<T>(it) }
}

/**
 * Response class which can be [Success] or [Error]
 */
sealed class Response<T> {
  class Success<T>(val value: T) : Response<T>()
  class Error<T>(val error: Throwable) : Response<T>()
}

/**
 * Apply log in filter to [Observable]
 * if there is no user logged in, the observable stop
 *
 * @param context Context to check the user
 * @return [Observable] with filter applied
 */
fun <T> Observable<T>.filterLoggedIn(context: BaseActivity): Observable<T> {
  return this
      .map {
        val needLogin = if (context.service.currentUser == null) {
          val dialog = LoginDialog()
          dialog.show(context.supportFragmentManager, "login")
          false
        } else true
        Pair(it, needLogin)
      }
      .filter { it.second }
      .map { it.first }
}