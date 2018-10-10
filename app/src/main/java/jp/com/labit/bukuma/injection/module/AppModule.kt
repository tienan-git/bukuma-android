package jp.com.labit.bukuma.injection.module

import android.app.Application
import dagger.Module
import dagger.Provides
import jp.com.labit.bukuma.BukumaConfig
import jp.com.labit.bukuma.BukumaPreference
import jp.com.labit.bukuma.analytic.BukumaAnalytic
import jp.com.labit.bukuma.manager.IconBadgeManager
import jp.com.labit.bukuma.api.BukumaService
import jp.com.labit.bukuma.api.interceptor.ResponseInterceptor
import jp.com.labit.bukuma.manager.MaintenanceManager
import javax.inject.Singleton

/**
 * Created by zoonooz on 9/12/2016 AD.
 * App module provide application object
 */
@Module
class AppModule(val application: Application) {

  @Provides
  fun provideApplication(): Application {
    return application
  }

  @Provides
  @Singleton
  fun provideAppConfig(): BukumaConfig {
    return BukumaConfig(application)
  }

  @Provides
  @Singleton
  fun providePreference(): BukumaPreference {
    return BukumaPreference(application)
  }

  @Provides
  @Singleton
  fun provideAnalytic(): BukumaAnalytic {
    return BukumaAnalytic(application)
  }

  @Provides
  @Singleton
  fun provideIconBadgeManager(): IconBadgeManager {
    return IconBadgeManager(application)
  }

  @Provides
  @Singleton
  fun provideMaintenanceManager(
      service: BukumaService,
      responseInterceptor: ResponseInterceptor): MaintenanceManager {
    return MaintenanceManager(service, responseInterceptor)
  }
}
