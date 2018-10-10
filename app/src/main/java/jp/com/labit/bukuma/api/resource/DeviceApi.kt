package jp.com.labit.bukuma.api.resource

import android.content.Context
import android.os.Build
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.firebase.iid.FirebaseInstanceId
import jp.com.labit.bukuma.BukumaConfig
import jp.com.labit.bukuma.BukumaPreference
import jp.com.labit.bukuma.api.BukumaService
import jp.com.labit.bukuma.api.response.BaseResponse
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber


/**
 * Created by zoonooz on 11/2/2016 AD.
 * Device api
 */
class DeviceApi(val service: BukumaService,
                val config: BukumaConfig,
                val context: Context,
                val preference: BukumaPreference) {

  /**
   * Send token to server for FCM
   *
   * 1. get token
   * 2. check if it is the same with last time
   * 3. if not, send to server
   *
   * @return [Single] of [BaseResponse] indicating the result or null if cannot get user or token
   */
  fun sendToken(): Single<BaseResponse>? {
    val token = FirebaseInstanceId.getInstance().token
    Timber.i("FCM token : $token")
    if (token == null || service.currentUser == null) return null
    // no need to save if it is the same with last sent token
    if (token == preference.lastFcmToken) return null
    return service.api.sendToken(token, config.uuid)
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        // save last token
        .doOnSuccess { preference.lastFcmToken = token }
  }

  /**
   * Send device informations to server
   */
  fun sendInfo(): Single<BaseResponse>? {
    var token: String? = null
    var advertisingId: String? = null
    var appVersion: String? = null
    var osVersion: String? = null

    return Single.create <Unit> {
      token = FirebaseInstanceId.getInstance().token
      Timber.d("FCM token : $token")

      val advertisingIdInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
      if (advertisingIdInfo != null) {
        if (advertisingIdInfo.isLimitAdTrackingEnabled) {
          Timber.d("Limit AD Tracking Enabled")
        } else {
          advertisingId = advertisingIdInfo?.id
          Timber.d("advertising id : $advertisingId")
        }
      }

      val packageInfo = context.packageManager?.getPackageInfo(context.packageName, 0)
      appVersion = "${packageInfo?.versionName}(${packageInfo?.versionCode})"
      osVersion = "${Build.VERSION.RELEASE}(${Build.VERSION.SDK_INT})"

      it.onSuccess(Unit)
    }
    .flatMap {
      service.api.sendDevice(config.uuid, token, advertisingId, appVersion, osVersion)
    }
    .subscribeOn(Schedulers.newThread())
    .observeOn(AndroidSchedulers.mainThread())
  }
}
