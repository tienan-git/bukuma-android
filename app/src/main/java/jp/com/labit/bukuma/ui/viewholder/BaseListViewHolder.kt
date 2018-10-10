package jp.com.labit.bukuma.ui.viewholder

import android.graphics.Rect
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.api.BukumaApi
import jp.com.labit.bukuma.ui.adapter.BaseSimpleAdapter
import jp.com.labit.bukuma.ui.custom.RecyclerViewScrollEndListener
import jp.com.labit.bukuma.util.dpToPx
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*

/**
 * Created by zoonooz on 11/16/2016 AD.
 * Base view holder for list. Handle sub recyclerview inside.
 * Show horizontally sub list item
 */
abstract class BaseListViewHolder<T, E : BaseObjectViewHolder<T>>(
    parent: ViewGroup,
    subHolderClass: Class<E>) : BaseObjectViewHolder<T>(parent, R.layout.viewholder_list) {

  val recyclerView = itemView.findViewById(R.id.recyclerview) as RecyclerView
  val adapter = BaseSimpleAdapter(subHolderClass)
  var scrollEndListener: RecyclerViewScrollEndListener

  init {
    val layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
    val itemDecor = object : RecyclerView.ItemDecoration() {
      override fun getItemOffsets(
          outRect: Rect?,
          view: View?,
          parent: RecyclerView,
          state: RecyclerView.State?) {
        outRect?.right = dpToPx(parent.resources, 16f)
      }
    }
    recyclerView.layoutManager = layoutManager
    recyclerView.addItemDecoration(itemDecor)
    recyclerView.adapter = adapter

    adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
      override fun onChanged() {
        recyclerView.visibility = if (adapter.itemCount == 0) View.GONE else View.VISIBLE
      }
    })

    // detect scroll end to load next page
    scrollEndListener = object : RecyclerViewScrollEndListener(layoutManager) {
      override fun onLoadMore(current: Int) {
        fetch(current + BukumaApi.START_PAGE)
      }
    }
    recyclerView.addOnScrollListener(scrollEndListener)
  }

  fun load() {
    fetch()
  }

  // fetch data

  open fun fetchStream(page: Int): Single<List<T>>? {
    return null
  }

  fun fetch(page: Int = BukumaApi.START_PAGE) {
    if (page == BukumaApi.START_PAGE) scrollEndListener.reset()

    val stream = fetchStream(page)
    if (stream == null) {
      recyclerView.visibility = View.GONE
    }

    stream?.let {
      it.subscribeOn(Schedulers.newThread())
          .observeOn(AndroidSchedulers.mainThread())
          .takeUntil(RxView.detaches(recyclerView))
          .subscribe({
            if (page == BukumaApi.START_PAGE) {
              adapter.items = ArrayList()
            }
            adapter.items.addAll(it)
            adapter.notifyDataSetChanged()
          }, {})
    }
  }

  override fun setObject(obj: T) {
    // do nothing
  }
}