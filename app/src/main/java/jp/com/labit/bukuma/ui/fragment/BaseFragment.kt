package jp.com.labit.bukuma.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import com.squareup.picasso.Picasso
import jp.com.labit.bukuma.BukumaApplication
import jp.com.labit.bukuma.BukumaPreference
import jp.com.labit.bukuma.analytic.BukumaAnalytic
import jp.com.labit.bukuma.api.BukumaService
import jp.com.labit.bukuma.ui.activity.BaseActivity
import javax.inject.Inject

/**
 * Created by zoonooz on 9/8/2016 AD.
 * Base class for all fragment
 * - inject dependency
 */
abstract class BaseFragment : Fragment() {

  @Inject lateinit var service: BukumaService
  @Inject lateinit var picasso: Picasso
  @Inject lateinit var preference: BukumaPreference
  @Inject lateinit var tracker: BukumaAnalytic

  /**
   * get current base activity
   */
  val baseActivity: BaseActivity get() { return activity as BaseActivity }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    BukumaApplication.mainComponent.inject(this)
  }
}
