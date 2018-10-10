package jp.com.labit.bukuma

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import io.realm.RealmConfiguration
import jp.com.labit.bukuma.analytic.BukumaAnalytic
import jp.com.labit.bukuma.api.BukumaService
import jp.com.labit.bukuma.database.Migration
import jp.com.labit.bukuma.fcm.NotificationInfo
import jp.com.labit.bukuma.injection.DaggerMainComponent
import jp.com.labit.bukuma.injection.MainComponent
import jp.com.labit.bukuma.injection.module.AppModule
import jp.com.labit.bukuma.injection.module.MediaModule
import jp.com.labit.bukuma.injection.module.NetworkModule
import jp.com.labit.bukuma.manager.IconBadgeManager
import jp.com.labit.bukuma.util.FabricTree
import jp.com.labit.bukuma.util.StethoUtils
import rx.Observable
import rx.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by zoonooz on 9/12/2016 AD.
 * Bukuma application class
 */
class BukumaApplication : Application() {

  companion object {
    @JvmStatic
    lateinit var mainComponent: MainComponent
  }

  @Inject lateinit var service: BukumaService
  @Inject lateinit var analytic: BukumaAnalytic
  @Inject lateinit var iconBadgeManager: IconBadgeManager

  /**
   * Stream event when got push notification
   */
  lateinit var notificationObservable: PublishSubject<NotificationInfo>

  override fun onCreate() {
    super.onCreate()

    // debug
    Fabric.with(this, Answers(), Crashlytics())
    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }
    Timber.plant(FabricTree())
    StethoUtils.init(this)

    // dagger component
    mainComponent = DaggerMainComponent.builder()
        .appModule(AppModule(this))
        .mediaModule(MediaModule(this))
        .networkModule(NetworkModule())
        .build()
    mainComponent.inject(this)

    analytic.start()
    setupRealm()
    setupNotification()
    service.getInitialDataIfNeed().subscribe({}, {})

    registerActivityLifecycleCallbacks(BukumaLifeCycle())

    sendDeviceInfo()
  }

  private fun setupRealm() {
    val config = RealmConfiguration.Builder(this)
        .name("bkm")
        .schemaVersion(1)
        .migration(Migration())
        .build()
    Realm.setDefaultConfiguration(config)
  }

  private fun setupNotification() {
    notificationObservable = PublishSubject.create()
    iconBadgeManager.setup(this)
  }

  private fun sendDeviceInfo() {
    service.devices.sendInfo()?.subscribe({
      Timber.i("send device info to server succeed")
    }, {
      Timber.e(it, "send device info to server failed")
    })
  }

  // multi dex

  override fun attachBaseContext(base: Context?) {
    super.attachBaseContext(base)
    MultiDex.install(this)
  }

  // push noti next event

  /**
   * Broadcast notification to subscriber
   * @param type notification type
   */
  fun postNotification(info: NotificationInfo) {
    notificationObservable.onNext(info)
  }
}
