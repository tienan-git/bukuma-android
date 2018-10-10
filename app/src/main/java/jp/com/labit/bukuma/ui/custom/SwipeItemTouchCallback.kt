package jp.com.labit.bukuma.ui.custom

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.ui.adapter.BaseAdapter
import jp.com.labit.bukuma.util.dpToPx

/**
 * Created by zoonooz on 10/3/2016 AD.
 * ItemTouchHelper callback for remove item
 */
class SwipeItemTouchCallback<T>(val adapter: BaseAdapter<T>, val holderClass: Class<*>? = null) :
    ItemTouchHelper.Callback() {

  private val backgroundPaint = Paint()
  var onItemSwiped: (adapter: BaseAdapter<T>, T, Int, Int) -> Unit = { a, i, p, rp -> }

  init {
    backgroundPaint.color = Color.RED
  }

  override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder): Int {
    if (holderClass != null && !holderClass.isInstance(viewHolder)) {
      return makeMovementFlags(0, 0)
    }
    return makeMovementFlags(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
  }

  override fun onMove(
      recyclerView: RecyclerView?,
      viewHolder: RecyclerView.ViewHolder?,
      target: RecyclerView.ViewHolder?): Boolean {
    return false
  }

  override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    val position = viewHolder.adapterPosition
    val realPosition = adapter.getRealItemPosition(position)
    val item = adapter.getItem(position)
    adapter.items.removeAt(realPosition)
    adapter.notifyItemRemoved(position)
    onItemSwiped(adapter, item, position, realPosition)
  }

  override fun onChildDraw(
      c: Canvas,
      recyclerView: RecyclerView,
      viewHolder: RecyclerView.ViewHolder,
      dX: Float, dY: Float,
      actionState: Int,
      isCurrentlyActive: Boolean) {
    val itemView = viewHolder.itemView
    c.drawRect(itemView.left.toFloat(),
        itemView.top.toFloat(),
        itemView.right.toFloat(),
        itemView.bottom.toFloat(),
        backgroundPaint)

    val icon = ContextCompat.getDrawable(itemView.context, R.drawable.ic_delete_white_24dp)
    val size = dpToPx(itemView.context.resources, 24f)
    val margin = dpToPx(itemView.context.resources, 16f)
    val top = itemView.top + (itemView.height - size) / 2
    if (dX > 0) {
      icon.setBounds(margin, top, margin + size, top + size)
    } else {
      icon.setBounds(
          itemView.right - margin - size,
          top,
          itemView.right - margin,
          top + size)
    }
    icon.draw(c)
    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
  }
}