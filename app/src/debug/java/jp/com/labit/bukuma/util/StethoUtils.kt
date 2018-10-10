package jp.com.labit.bukuma.util

import android.app.Application
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.uphyca.stetho_realm.RealmInspectorModulesProvider
import okhttp3.Interceptor
import java.util.regex.Pattern

/**
 * Created by zoonooz on 9/12/2016 AD.
 * Stetho util for debugging only
 */
class StethoUtils {
  companion object {
    fun init(application: Application) {
      Stetho.initialize(
              Stetho.newInitializerBuilder(application)
                      .enableDumpapp(Stetho.defaultDumperPluginsProvider(application))
                      .enableWebKitInspector(RealmInspectorModulesProvider
                              .builder(application)
                              .databaseNamePattern(Pattern.compile("bkm"))
                              .withLimit(1000)
                              .build())
                      .build()
      )
    }

    fun networkInterceptor(): Interceptor? {
      return StethoInterceptor()
    }
  }
}