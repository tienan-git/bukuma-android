package jp.com.labit.bukuma.manager

import jp.com.labit.bukuma.api.BukumaService
import jp.com.labit.bukuma.api.interceptor.ResponseInterceptor
import rx.subjects.PublishSubject
import java.net.HttpURLConnection

/**
 * Created by tani on 2017/06/15.
 */

class MaintenanceManager(val service: BukumaService, responseInterceptor: ResponseInterceptor) {

  val maintenanceSubject: PublishSubject<Unit> = PublishSubject.create<Unit>()

  val isUnderMaintenance: Boolean
    get() = isMaintenance || isLegacyMaintenance

  var isMaintenance = false
  var isLegacyMaintenance = false

  init {
    responseInterceptor.subject.subscribe { response ->
      if (response.code() == HttpURLConnection.HTTP_UNAVAILABLE) {
        startMaintenance()
      }
    }
  }

  fun startMaintenance() {
    isMaintenance = true
    notifyStartMaintenance()
  }

  fun finishMaintenance() {
    isMaintenance = false
    if (isLegacyMaintenance) {
      return
    }
    service.getInitialDataIfNeed().subscribe({}, {})
    notifyFinishMaintenance()
  }

  fun startLegacyMaintenance() {
    isLegacyMaintenance = true
    notifyStartMaintenance()
  }

  fun finishLegacyMaintenance() {
    isLegacyMaintenance = false
    if (isMaintenance) {
      return
    }
    notifyFinishMaintenance()
  }

  private fun notifyStartMaintenance() {
    maintenanceSubject.onNext(null)
  }

  private fun notifyFinishMaintenance() {
    maintenanceSubject.onNext(null)
  }
}