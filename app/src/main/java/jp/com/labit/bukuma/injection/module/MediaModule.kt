package jp.com.labit.bukuma.injection.module

import android.app.Application
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import jp.com.labit.bukuma.manager.ImageChoosingManager

/**
 * Created by zoonooz on 9/23/2016 AD.
 * Multimedia module
 */
@Module
class MediaModule(val application: Application) {

  @Provides
  fun provideImageLoader(): Picasso {
    return Picasso.with(application)
  }

  @Provides
  fun provideImageChooser(): ImageChoosingManager {
    return ImageChoosingManager(application)
  }
}
