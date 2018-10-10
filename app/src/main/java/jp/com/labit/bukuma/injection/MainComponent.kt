package jp.com.labit.bukuma.injection

import dagger.Component
import jp.com.labit.bukuma.BukumaApplication
import jp.com.labit.bukuma.BukumaLifeCycle
import jp.com.labit.bukuma.injection.module.AppModule
import jp.com.labit.bukuma.injection.module.MediaModule
import jp.com.labit.bukuma.injection.module.NetworkModule
import jp.com.labit.bukuma.ui.activity.BaseActivity
import jp.com.labit.bukuma.ui.activity.BaseImageChooserActivity
import jp.com.labit.bukuma.ui.dialog.BaseDialog
import jp.com.labit.bukuma.ui.fragment.BaseFragment
import jp.com.labit.bukuma.ui.viewholder.BaseViewHolder
import javax.inject.Singleton

/**
 * Created by zoonooz on 9/12/2016 AD.
 * Network component to define inject
 */
@Singleton
@Component(modules = arrayOf(AppModule::class, NetworkModule::class, MediaModule::class))
interface MainComponent {
  fun inject(application: BukumaApplication)
  fun inject(lifeCycle: BukumaLifeCycle)
  fun inject(activity: BaseActivity)
  fun inject(fragment: BaseFragment)
  fun inject(dialog: BaseDialog)
  fun inject(viewHolder: BaseViewHolder)

  // image chooser base
  fun inject(imageChooserActivity: BaseImageChooserActivity)
}
