package jp.com.labit.bukuma.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.toJson
import jp.com.labit.bukuma.model.Merchandise
import jp.com.labit.bukuma.ui.fragment.MerchandiseChooserFragment
import kotlinx.android.synthetic.main.activity_merchandise_chooser.*
import timber.log.Timber

/**
 * Created by zoonooz on 11/2/2016 AD.
 * Merchandise chooser
 */
class MerchandiseChooserActivity : BaseActivity(), MerchandiseChooserFragment.Callback {

  var userId = 0

  companion object {
    val EXTRA_USER_ID = "extra_user_id"
    val RESULT_MERCHANDISE = "result_merchandise"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_merchandise_chooser)

    supportActionBar?.title = ""
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    viewpager.adapter = PagerAdapter(supportFragmentManager)
    tab_layout.setupWithViewPager(viewpager)

    userId = intent.getIntExtra(EXTRA_USER_ID, 0)
  }

  inner class PagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
    override fun getCount(): Int {
      return 2
    }

    override fun getItem(position: Int): Fragment {
      return if (position == 0) MerchandiseChooserFragment.newInstance(userId)
      else MerchandiseChooserFragment.newInstance()
    }

    override fun getPageTitle(position: Int): CharSequence {
      return if (position == 0) getString(R.string.merchandise_chooser_tab_opponent)
      else getString(R.string.merchandise_chooser_tab_mine)
    }
  }

  // merchandise fragment callback

  override fun onMerchandiseSelect(merchandise: Merchandise) {
    Timber.i("choose merchandise id : ${merchandise.id}")
    val data = Intent()
    data.putExtra(RESULT_MERCHANDISE, merchandise.toJson())
    setResult(Activity.RESULT_OK, data)
    finish()
  }
}
