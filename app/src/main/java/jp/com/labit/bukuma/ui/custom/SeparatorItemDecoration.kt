package jp.com.labit.bukuma.ui.custom

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import jp.com.labit.bukuma.util.dpToPx

/**
 * Created by zoonooz on 2017/01/18.
 * Recycler view item separator
 */
class SeparatorItemDecoration(val height: Float = 1.5f) : RecyclerView.ItemDecoration() {
  override fun getItemOffsets(
      outRect: Rect?,
      view: View?,
      parent: RecyclerView,
      state: RecyclerView.State?) {
    outRect?.bottom = dpToPx(parent.resources, height)
  }
}