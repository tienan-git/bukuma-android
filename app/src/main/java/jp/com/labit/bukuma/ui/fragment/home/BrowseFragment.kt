package jp.com.labit.bukuma.ui.fragment.home

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.SparseArray
import android.view.*
import com.jakewharton.rxbinding.view.RxMenuItem
import com.jakewharton.rxbinding.view.RxView
import io.realm.Realm
import io.realm.RealmResults
import jp.com.labit.bukuma.BukumaApplication
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.filterLoggedIn
import jp.com.labit.bukuma.fcm.NotificationCreator
import jp.com.labit.bukuma.model.realm.Tab
import jp.com.labit.bukuma.ui.activity.ActivitiesActivity
import jp.com.labit.bukuma.ui.activity.MainActivity
import jp.com.labit.bukuma.ui.activity.TodoActivity
import jp.com.labit.bukuma.ui.fragment.BaseBookListFragment
import jp.com.labit.bukuma.ui.fragment.BaseFragment
import jp.com.labit.bukuma.ui.fragment.BookTabFragment
import kotlinx.android.synthetic.main.fragment_browse.*
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 9/9/2016 AD.
 * Book browse fragment
 */
class BrowseFragment : BaseFragment() {

  lateinit var realm: Realm
  lateinit var tabs: RealmResults<Tab>
  lateinit var adapter: PagerAdapter

  private var hasTodo = false
  private var hasUnreadActivities = false
  private var notiSubscription: Subscription? = null

  companion object {
    val pageCount = 100
    val initialPage = pageCount / 2
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)

    realm = Realm.getDefaultInstance()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    notiSubscription = (activity.application as BukumaApplication)
        .notificationObservable
        .observeOn(AndroidSchedulers.mainThread())
        .filter { it.type != NotificationCreator.Type.MESSAGE }
        .subscribe { updateBadge() }
    return inflater.inflate(R.layout.fragment_browse, container, false)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    tabs = realm.where(Tab::class.java).findAll()
    tabs.addChangeListener {
      adapter.notifyDataSetChanged()
      tab_layout.post {
        selectInitialPage()
      }
    }

    adapter = PagerAdapter(childFragmentManager, tabs)
    viewpager.adapter = adapter
    tab_layout.setupWithViewPager(viewpager)

    if (!tabs.isEmpty()) {
      selectInitialPage()
    }

    // get transaction count
    updateBadge()
  }

  override fun onDestroyView() {
    tabs.removeChangeListeners()
    notiSubscription?.unsubscribe()
    super.onDestroyView()
  }

  override fun onDestroy() {
    realm.close()
    super.onDestroy()
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
    super.onCreateOptionsMenu(menu, inflater)
    inflater?.inflate(R.menu.menu_browse, menu)

    // badge
    menu.findItem(R.id.checked).setIcon(
        if (hasTodo) R.drawable.ic_nav_check_noti
        else R.drawable.ic_nav_check)
    menu.findItem(R.id.notification).setIcon(
        if (hasUnreadActivities) R.drawable.ic_nav_bell_noti
        else R.drawable.ic_nav_bell)

    RxMenuItem.clicks(menu.findItem(R.id.notification))
        .throttleFirst(1, TimeUnit.SECONDS)
        .filterLoggedIn(baseActivity)
        .subscribe {
          // show notification activity
          hasUnreadActivities = false
          activity?.supportInvalidateOptionsMenu()
          startActivity(Intent(activity, ActivitiesActivity::class.java))
        }

    RxMenuItem.clicks(menu.findItem(R.id.checked))
        .throttleFirst(1, TimeUnit.SECONDS)
        .filterLoggedIn(baseActivity)
        .subscribe {
          // show Todolist activity
          startActivity(Intent(activity, TodoActivity::class.java))
        }

    RxMenuItem.clicks(menu.findItem(R.id.search))
        .throttleFirst(1, TimeUnit.SECONDS)
        .subscribe { (activity as? MainActivity)?.showSearch() }
  }

  fun updateBadge() {
    service.api.getTransactionCount()
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .takeUntil(RxView.detaches(tab_layout))
        .subscribe({
          Timber.i("check todo badge count success")
          hasTodo = it.count > 0
          activity?.supportInvalidateOptionsMenu()
        }, {
          Timber.e(it, "check todo badge count error")
        })

    service.api.getActivityCount(preference.lastActivitiesId)
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .takeUntil(RxView.detaches(tab_layout))
        .subscribe({
          Timber.i("check activity badge count success")
          hasUnreadActivities = it.count > 0
          activity?.supportInvalidateOptionsMenu()
        }, {
          Timber.e(it, "check activity badge count error")
        })
  }

  private fun selectInitialPage() {
    viewpager.setCurrentItem(initialPage, false)
    tab_layout.getTabAt(viewpager.currentItem)?.select()
  }

  // pager adapter

  inner class PagerAdapter(
      fragmentManager: FragmentManager,
      var tabs: List<Tab>) : FragmentPagerAdapter(fragmentManager) {

    var fragments = SparseArray<BaseBookListFragment>()

    override fun getCount(): Int {
      return if(tabs.isEmpty()) 0 else pageCount
    }

    override fun getPageTitle(position: Int): CharSequence {
      return tabs[dataIndex(position)].name
    }

    override fun getItem(position: Int): Fragment {
      val index = dataIndex(position)
      val tab = tabs[index]
      return BookTabFragment.newInstance(tab.id, index)
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): Any {
      val fragment = super.instantiateItem(container, position)
      fragments.put(position, fragment as BaseBookListFragment)
      return fragment
    }

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
      fragments.remove(position)
      super.destroyItem(container, position, `object`)
    }

    fun getFragment(position: Int): BaseBookListFragment? {
      return fragments.get(position)
    }

    private fun dataIndex(position: Int): Int {
      var index = (position - initialPage) % tabs.size
      if(index < 0) index += tabs.size
      return index
    }
  }
}
