package jp.com.labit.bukuma.util

import android.util.Log
import com.crashlytics.android.Crashlytics
import timber.log.Timber

/**
 * Created by zoonooz on 11/2/2016 AD.
 * Fabric log tree for Timber
 */
class FabricTree : Timber.Tree() {

  override fun log(priority: Int, tag: String?, message: String?, t: Throwable?) {
    if (priority == Log.VERBOSE || priority == Log.DEBUG) {
      return
    }

    Crashlytics.setInt("priority", priority)
    Crashlytics.setString("tag", tag)
    Crashlytics.setString("message", message)

    if (t == null) {
      Crashlytics.log(message)
    } else {
      Crashlytics.logException(t)
    }
  }
}