package jp.com.labit.bukuma.api.resource

import io.realm.Realm
import jp.com.labit.bukuma.BukumaConfig
import jp.com.labit.bukuma.api.BukumaService
import jp.com.labit.bukuma.extension.realm
import jp.com.labit.bukuma.model.realm.Tab
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by tani on 2017/05/08.
 */
class TabApi(var service: BukumaService, var config: BukumaConfig) {

  private var needFetchingTabs = true

  fun getTabs(): Single<List<Tab>> {
    Realm.getDefaultInstance().executeTransaction {
      it.where(Tab::class.java).findAll().deleteAllFromRealm()
    }
    return service.api.getTabs()
        .map { it.convert() }
        .doOnSuccess { ret -> realm { it.copyToRealm(ret) } }
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
  }

  fun getTabsIfNeed(): Single<Unit> {
    if (!needFetchingTabs) {
      return Single.create { null }
    }

    return getTabs()
        .doOnSuccess { needFetchingTabs = false }
        .map { null }
  }
}