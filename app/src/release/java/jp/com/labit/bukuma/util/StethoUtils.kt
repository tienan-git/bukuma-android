package jp.com.labit.bukuma.util

import android.app.Application
import okhttp3.Interceptor

/**
 * Created by zoonooz on 9/12/2016 AD.
 * Stetho util for debugging only
 */
class StethoUtils {
  companion object {
    fun init(application: Application) {
    }

    fun networkInterceptor(): Interceptor? {
      return null
    }
  }
}