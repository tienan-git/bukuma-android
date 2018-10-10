package jp.com.labit.bukuma.analytic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import jp.com.labit.bukuma.BukumaApplication

/**
 * Created by zoonooz on 12/16/2016 AD.
 * Install referrer receiver
 */
class ReferrerReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent?) {
    val appContext = context.applicationContext as BukumaApplication
    appContext.analytic.trackInstall(intent)
  }
}
