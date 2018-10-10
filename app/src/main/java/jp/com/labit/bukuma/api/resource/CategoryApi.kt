package jp.com.labit.bukuma.api.resource

import jp.com.labit.bukuma.BukumaConfig
import jp.com.labit.bukuma.api.BukumaService
import jp.com.labit.bukuma.extension.realm
import jp.com.labit.bukuma.model.realm.Category
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by zoonooz on 10/24/2016 AD.
 * Category api
 */
class CategoryApi(var service: BukumaService, var config: BukumaConfig) {

  private var needFetchingCategories = true

  /**
   * Get all categories from server and create local categories
   *
   * 1. get categories
   * 2. add local categories
   * 3. save to db
   *
   * @return [Single] of list of [Category]
   */
  fun getCategories(): Single<List<Category>> {
    return service.api.getCategories()
        .map { it.convert() }
        .doOnSuccess { r -> realm { it.copyToRealmOrUpdate(r) } }
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
  }

  fun getCategoriesIfNeed(): Single<Unit> {
    if (!needFetchingCategories) {
      return Single.create { null }
    }

    return getCategories()
        .doOnSuccess { needFetchingCategories = false }
        .map { null }
  }
}
