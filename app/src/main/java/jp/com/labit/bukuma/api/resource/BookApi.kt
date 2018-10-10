package jp.com.labit.bukuma.api.resource

import jp.com.labit.bukuma.BukumaConfig
import jp.com.labit.bukuma.api.BukumaService
import jp.com.labit.bukuma.api.response.GetSearchOrderResponse
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Created by zoonooz on 10/19/2016 AD.
 * Book api
 */
class BookApi(var service: BukumaService, var config: BukumaConfig) {

  /**
   * Create search order and polling the status until found result or error
   *
   * 1. create search order
   * 2. check search order status every few second
   * 3. return when order finished or timeout
   *
   * @param query search text
   * @return observable of [GetSearchOrderResponse]
   * @exception IllegalStateException if create search order is null from server
   * @exception TimeoutException if more than 80 seconds and order is not finished
   */
  fun searchOrder(query: String): Observable<GetSearchOrderResponse> {
    return service.api.createSearchOrder(query)
        .flatMapObservable {
          if (it.searchOrder == null) throw IllegalStateException()
          val orderId = it.searchOrder!!.id
          Observable.interval(4, TimeUnit.SECONDS)
              .doOnNext { if (it > 6) throw TimeoutException() }
              .flatMap { service.api.getSearchOrder(orderId).toObservable() }
              .takeUntil { it.searchOrder?.finished == true }
              .filter { it.searchOrder?.finished == true }
        }
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
  }
}
