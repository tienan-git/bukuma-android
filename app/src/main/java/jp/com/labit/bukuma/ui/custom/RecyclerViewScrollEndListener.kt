package jp.com.labit.bukuma.ui.custom

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager

/**
 * Created by zoonooz on 9/8/2016 AD.
 * Scroll end listener use for load next page
 */
abstract class RecyclerViewScrollEndListener(lm: RecyclerView.LayoutManager) : RecyclerView.OnScrollListener() {
  private val layoutManager = lm
  private var firstVisibleItem = 0
  private var visibleItemCount = 0
  private var totalItemCount = 0
  private var previousTotal = 0
  private var current = 0
  private var loading = true

  /**
   * Implement this to handle when recyclerview needs to load more
   * @param current page index
   */
  abstract fun onLoadMore(current: Int)

  /**
   * Reset all states. must call this when load the fist page
   */
  fun reset() {
    current = 0
    firstVisibleItem = 0
    visibleItemCount = 0
    totalItemCount = 0
    previousTotal = 0
  }

  override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
    super.onScrolled(recyclerView, dx, dy)

    visibleItemCount = recyclerView.childCount
    totalItemCount = layoutManager.itemCount

    if (layoutManager is LinearLayoutManager) {
      firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
    } else if (layoutManager is StaggeredGridLayoutManager) {
      val items = layoutManager.findFirstVisibleItemPositions(null)
      firstVisibleItem = items[0]
    }

    if (loading) {
      if (totalItemCount > previousTotal) {
        loading = false
        previousTotal = totalItemCount
      }
    }

    if (totalItemCount < 5) return
    if (!loading && totalItemCount - visibleItemCount <= firstVisibleItem + visibleItemCount) {
      current++
      onLoadMore(current)
      loading = true
      previousTotal = totalItemCount
    }
  }
}
