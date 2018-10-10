package jp.com.labit.bukuma.fcm

import com.google.firebase.iid.FirebaseInstanceIdService
import jp.com.labit.bukuma.BukumaApplication
import timber.log.Timber

/**
 * Created by zoonooz on 11/2/2016 AD.
 * Firebase instance id
 */
class InstanceIdService : FirebaseInstanceIdService() {

  override fun onTokenRefresh() {
    (application as BukumaApplication).service.devices.sendToken()?.subscribe({
      Timber.i("send token to server success")
    }, {
      Timber.e(it, "send token to server failed")
    })
  }
}
