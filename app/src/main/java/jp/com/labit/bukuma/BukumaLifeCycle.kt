package jp.com.labit.bukuma

import android.app.Activity
import android.app.Application
import android.os.Bundle
import jp.com.labit.bukuma.analytic.BukumaAnalytic
import jp.com.labit.bukuma.api.BukumaService
import jp.com.labit.bukuma.manager.IconBadgeManager
import jp.com.labit.bukuma.manager.MaintenanceManager
import jp.com.labit.bukuma.ui.activity.MainActivity
import jp.com.labit.bukuma.util.RxAlertDialog
import jp.com.labit.bukuma.util.openAppInStore
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by zoonooz on 9/20/2016 AD.
 * Inject some logic to activity here
 */
class BukumaLifeCycle : Application.ActivityLifecycleCallbacks {

  @Inject lateinit var service: BukumaService
  @Inject lateinit var config: BukumaConfig
  @Inject lateinit var tracker: BukumaAnalytic
  @Inject lateinit var iconBadgeManager: IconBadgeManager
  @Inject lateinit var maintenanceManager: MaintenanceManager

  init {
    BukumaApplication.mainComponent.inject(this)
  }

  override fun onActivityPaused(activity: Activity) {
  }

  override fun onActivityStarted(activity: Activity) {
  }

  override fun onActivityDestroyed(activity: Activity) {
  }

  override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle?) {
  }

  override fun onActivityStopped(activity: Activity) {
  }

  override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
    if (activity is MainActivity) {

      // update new version
      val localVersion = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionCode
      val latestVersion = config.latestVersion
      if (localVersion < latestVersion) {
        RxAlertDialog.alert2(activity,
            activity.getString(R.string.dialog_update_title),
            activity.getString(R.string.dialog_update_message),
            activity.getString(R.string.dialog_update_now),
            activity.getString(R.string.dialog_update_later))
            .filter { it }
            .subscribe { openAppInStore(activity) }
      }
    }
  }

  override fun onActivityResumed(activity: Activity) {
    tracker.trackScreenView(activity.localClassName)
    iconBadgeManager.hideBadge()

    if (activity is MainActivity) {
      service.getAppConfig().subscribe({
        Timber.i("app config updated")

        // check maintenance
        if (config.isMaintenance) {
          maintenanceManager.startLegacyMaintenance()
        } else {
          maintenanceManager.finishLegacyMaintenance()
        }
      }, {
        Timber.w(it, "app config update error")
      })

      // update banner
      service.getAppBanners().subscribe({
        Timber.i("app banners updated")
      }, {
        Timber.e(it, "app banners update error")
      })
    }

    // check maintenance
    if (maintenanceManager.isMaintenance) {
      service.checkMaintenance().subscribe({
        maintenanceManager.finishMaintenance()
      }, {
        Timber.e(it)
      })
    }
  }
}
