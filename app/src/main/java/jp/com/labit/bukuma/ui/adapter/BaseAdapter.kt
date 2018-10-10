package jp.com.labit.bukuma.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.animation.AnimationUtils
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.api.BukumaApi
import java.util.*

/**
 * Created by zoonooz on 9/13/2016 AD.
 * Base adapter to items position and animation
 */
abstract class BaseAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  var items: MutableList<T> = ArrayList()
  open var animate = false

  private var lastAnimatePosition = -1

  override fun getItemCount(): Int {
    return items.size
  }

  /**
   * Return the object in [items] array using the display position.
   * If crash happened, probably because [getRealItemPosition] is not implemented correctly
   *
   * @param position display position
   * @return object
   */
  open fun getItem(position: Int): T {
    return items[getRealItemPosition(position)]
  }

  /**
   * Return real position on [items] array in case you add some header which make the display
   * position not match with data position
   *
   * @param position display position
   * @return real position in [items] array
   */
  open fun getRealItemPosition(position: Int): Int {
    return position
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    if (animate) {
      if (position > lastAnimatePosition) {
        val animation = AnimationUtils.loadAnimation(
            holder.itemView.context,
            R.anim.item_fade_in)
        holder.itemView.startAnimation(animation)
        lastAnimatePosition = position
      } else {
        holder.itemView.clearAnimation()
      }
    }
  }

  override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder?) {
    super.onViewDetachedFromWindow(holder)
    if (animate) holder?.itemView?.clearAnimation()
  }

  fun notifyDataSetChanged(page: Int) {
    if (page == BukumaApi.START_PAGE) {
      onDataRefreshed()
    }
    notifyDataSetChanged()
  }

  open protected fun onDataRefreshed() {}
}