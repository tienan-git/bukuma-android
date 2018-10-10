package jp.com.labit.bukuma.ui.custom

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.OrientationHelper
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Created by zoonooz on 11/25/2016 AD.
 * RecyclerView snap helper
 */
class HorizontalSnapHelper : LinearSnapHelper() {

  private var mHorizontalHelper: OrientationHelper? = null

  override fun findTargetSnapPosition(
      layoutManager: RecyclerView.LayoutManager,
      velocityX: Int, velocityY: Int): Int {
    val view = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION

    val position = layoutManager.getPosition(view)
    var targetPosition = -1
    if (layoutManager.canScrollHorizontally()) {
      if (velocityX < 0) {
        targetPosition = position - 1
      } else {
        targetPosition = position + 1
      }
    }

    if (layoutManager.canScrollVertically()) {
      if (velocityY < 0) {
        targetPosition = position - 1
      } else {
        targetPosition = position + 1
      }
    }

    val firstItem = 0
    val lastItem = layoutManager.itemCount - 1
    targetPosition = Math.min(lastItem, Math.max(targetPosition, firstItem))
    return targetPosition
  }

  override fun calculateDistanceToFinalSnap(
      layoutManager: RecyclerView.LayoutManager,
      targetView: View): IntArray? {
    val out = IntArray(2)

    if (layoutManager.canScrollHorizontally()) {
      out[0] = distanceToStart(targetView, getHorizontalHelper(layoutManager))
    }

    return out
  }

  override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
    return findStartView(layoutManager, getHorizontalHelper(layoutManager))
  }

  private fun findStartView(layoutManager: RecyclerView.LayoutManager, helper: OrientationHelper): View? {
    if (layoutManager is LinearLayoutManager) {
      val firstChild = layoutManager.findFirstVisibleItemPosition()

      if (firstChild == RecyclerView.NO_POSITION) {
        return null
      }

      val child = layoutManager.findViewByPosition(firstChild)

      val visibleWidth: Float

      // We should return the child if it's visible width
      // is greater than 0.5 of it's total width.
      visibleWidth = helper.getDecoratedEnd(child).toFloat() / helper.getDecoratedMeasurement(child)

      // If we're at the end of the list, we shouldn't snap
      // to avoid having the last item not completely visible.
      val endOfList = layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1

      if (visibleWidth > 0.5f && !endOfList) {
        return child
      } else if (endOfList) {
        return null
      } else {
        // If the child wasn't returned, we need to return
        // the next view close to the start.
        return layoutManager.findViewByPosition(firstChild + 1)
      }
    }

    return null
  }

  private fun distanceToStart(targetView: View, helper: OrientationHelper): Int {
    return helper.getDecoratedStart(targetView) - helper.startAfterPadding
  }

  private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
    if (mHorizontalHelper == null) {
      mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
    }
    return mHorizontalHelper!!
  }
}
