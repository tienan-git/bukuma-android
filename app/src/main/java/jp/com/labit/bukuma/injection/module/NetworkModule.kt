package jp.com.labit.bukuma.injection.module

import android.app.Application
import android.os.Build
import com.google.gson.*
import dagger.Module
import dagger.Provides
import io.realm.RealmObject
import jp.com.labit.bukuma.BuildConfig
import jp.com.labit.bukuma.BukumaConfig
import jp.com.labit.bukuma.BukumaPreference
import jp.com.labit.bukuma.api.BukumaApi
import jp.com.labit.bukuma.api.BukumaService
import jp.com.labit.bukuma.api.OmiseApi
import jp.com.labit.bukuma.api.interceptor.AccessTokenInterceptor
import jp.com.labit.bukuma.api.interceptor.ResponseInterceptor
import jp.com.labit.bukuma.api.util.TlsPatch
import jp.com.labit.bukuma.util.StethoUtils
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

/**
 * Created by zoonooz on 9/12/2016 AD.
 * Network module for networking
 */
@Module
class NetworkModule {

  @Provides
  fun provideGson(): Gson {
    val gsonBuilder = GsonBuilder()
    gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    gsonBuilder.setExclusionStrategies(object : ExclusionStrategy {
      override fun shouldSkipClass(clazz: Class<*>?): Boolean {
        return false
      }

      override fun shouldSkipField(f: FieldAttributes): Boolean {
        return f.declaringClass == RealmObject::class.java
      }
    })
    gsonBuilder.serializeNulls()
    return gsonBuilder.create()
  }

  @Provides
  fun provideOkHttpClientBuilder(): OkHttpClient.Builder {
    // http debug
    val loggingInterceptor = HttpLoggingInterceptor()
    loggingInterceptor.level = if (BuildConfig.DEBUG)
      HttpLoggingInterceptor.Level.BODY
    else
      HttpLoggingInterceptor.Level.NONE

    val client = OkHttpClient.Builder()
    client.addNetworkInterceptor(loggingInterceptor)
    StethoUtils.networkInterceptor()?.let { client.addNetworkInterceptor(it) }
    return client
  }

  // bukuma api

  @Provides
  fun provideBukumaApi(
      config: BukumaConfig,
      client: OkHttpClient.Builder,
      gson: Gson,
      responseInterceptor: ResponseInterceptor): BukumaApi {
    client.addNetworkInterceptor(AccessTokenInterceptor())
    client.addInterceptor(responseInterceptor)
    val retrofit = Retrofit.Builder()
        .baseUrl(config.baseUrl)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .client(client.build())
        .build()
    return retrofit.create(BukumaApi::class.java)
  }

  // omise api

  @Provides
  fun provideOmiseApi(client: OkHttpClient.Builder, gson: Gson): OmiseApi {
    // ssl version
    val connectionSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
        .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1)
        .build()
    client.connectionSpecs(listOf(connectionSpec))

    if (Build.VERSION.SDK_INT < 21) {
      val trustManager = TlsPatch.systemDefaultTrustManager()
      client.sslSocketFactory(TlsPatch.TLSSocketFactory(), trustManager)
    }

    val retrofit = Retrofit.Builder()
        .baseUrl("https://vault.omise.co/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .client(client.build())
        .build()
    return retrofit.create(OmiseApi::class.java)
  }

  // service

  @Provides
  @Singleton
  fun provideBukumaService(
      application: Application,
      api: BukumaApi,
      config: BukumaConfig,
      pref: BukumaPreference,
      omiseApi: OmiseApi): BukumaService {
    return BukumaService(application, api, config, pref, omiseApi)
  }

  @Provides
  @Singleton
  fun provideResponseInterceptor(): ResponseInterceptor {
    return ResponseInterceptor()
  }
}
