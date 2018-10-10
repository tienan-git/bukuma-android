package jp.com.labit.bukuma.ui.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.api.BukumaApi
import jp.com.labit.bukuma.ui.adapter.BaseAdapter
import jp.com.labit.bukuma.ui.custom.RecyclerViewScrollEndListener
import jp.com.labit.bukuma.ui.custom.SeparatorItemDecoration
import jp.com.labit.bukuma.util.dpToPx
import kotlinx.android.synthetic.main.fragment_list.*
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*

/**
 * Created by zoonooz on 9/8/2016 AD.
 * Base class for all list fragment
 */
abstract class BaseListFragment<T> : BaseFragment() {

  val START_PAGE = BukumaApi.START_PAGE

  /** title text for empty view */
  open val emptyTitleText: String? = null

  /** description text for empty view */
  open val emptyDescriptionText: String? = null

  /** image for empty view */
  open val emptyImageResourceId: Int = 0

  /** should fetch the data when activity resume or not */
  open val fetchWhenResume = false

  var scrollEndListener: RecyclerViewScrollEndListener? = null
  var fetchMoreOnScrollEnd = true

  val adapter: BaseAdapter<T>?
    get() = recyclerview?.adapter as? BaseAdapter<T>

  override fun onCreateView(inflater: LayoutInflater,
                            container: ViewGroup?,
                            savedInstanceState: Bundle?): View {
    return inflater.inflate(R.layout.fragment_list, container, false)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val layoutManager = layoutManager()
    recyclerview.layoutManager = layoutManager
    recyclerview.setHasFixedSize(true)
    recyclerview.isMotionEventSplittingEnabled = false
    itemDecoration()?.let { recyclerview.addItemDecoration(it) }

    // detect scroll end to load next page
    if (fetchMoreOnScrollEnd) {
      scrollEndListener = object : RecyclerViewScrollEndListener(layoutManager) {
        override fun onLoadMore(current: Int) {
          fetch(current + START_PAGE)
        }
      }
      recyclerview.addOnScrollListener(scrollEndListener)
    }

    // swipe refresh
    refresh_layout.setColorSchemeResources(R.color.colorPrimary)
    refresh_layout.setOnRefreshListener { fetch() }

    // set adapter if exist
    adapter()?.let {
      it.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
          if (it.itemCount == 0) showEmptyView() else hideEmptyView()
        }
      })
      recyclerview.adapter = it
      itemTouchCallback(it)?.let { ItemTouchHelper(it).attachToRecyclerView(recyclerview) }
    }

    // fetch once
    if (!fetchWhenResume) fetch()
  }

  override fun onResume() {
    super.onResume()
    if (fetchWhenResume) fetch()
  }

  // require object

  /**
   * Adapter data to display. Override to return subclass of [BaseAdapter]
   * @return adapter or null if no data to display
   */
  abstract fun adapter(): BaseAdapter<T>?

  /**
   * Layout manager
   * @return layout manager object. Default is [LinearLayoutManager]
   */
  open fun layoutManager(): RecyclerView.LayoutManager {
    return LinearLayoutManager(activity)
  }

  /**
   * Return touch helper if swipe functions needed
   * @param adapter current data adapter
   * @return [ItemTouchHelper.Callback]
   */
  open fun itemTouchCallback(adapter: BaseAdapter<T>): ItemTouchHelper.Callback? {
    return null
  }

  /**
   * Item decoration object. Default is object to add separator
   * @return [RecyclerView.ItemDecoration]
   */
  open fun itemDecoration(): RecyclerView.ItemDecoration? {
    return SeparatorItemDecoration()
  }

  // fetch data

  /**
   * Override and return the data stream for use to fetch the data
   * @param page page index
   * @return [Single] of list of data
   */
  open fun fetchStream(page: Int): Single<List<T>>? {
    return null
  }

  /**
   * Start fetching from [fetchStream]
   * @param page page index
   */
  open fun fetch(page: Int = START_PAGE) {
    if (page == START_PAGE) scrollEndListener?.reset()

    val stream = fetchStream(page)
    if (stream == null) {
      refresh_layout.isRefreshing = false
      showEmptyView()
    }

    stream?.let {
      it.subscribeOn(Schedulers.newThread())
          .observeOn(AndroidSchedulers.mainThread())
          .takeUntil(RxView.detaches(recyclerview))
          .doOnEach { refresh_layout.isRefreshing = false }
          .subscribe({ fetchDisplay(page, it) }, {})
    }
  }

  /**
   * This will call to assign result of [fetch] to adapter
   * @param page page index
   * @param list data list
   */
  open fun fetchDisplay(page: Int, list: List<T>) {
    @Suppress("UNCHECKED_CAST")
    val adapter = recyclerview.adapter as BaseAdapter<T>
    if (page == START_PAGE) {
      adapter.items = ArrayList()
    }
    adapter.items.addAll(list)
    adapter.notifyDataSetChanged(page)
  }

  // empty

  open fun showEmptyView() {
    empty_title_textview?.text = emptyTitleText
    empty_description_textview?.text = emptyDescriptionText
    empty_layout?.visibility = View.VISIBLE
    recyclerview?.setBackgroundResource(android.R.color.transparent)

    if (emptyImageResourceId != 0 && empty_imageview != null) {
      val size = dpToPx(resources, 160f)
      picasso.load(emptyImageResourceId)
          .resize(size, size)
          .centerInside()
          .into(empty_imageview)
    }
  }

  open fun hideEmptyView() {
    empty_layout?.visibility = View.INVISIBLE
    recyclerview?.setBackgroundResource(R.color.list_separator)
  }
}
