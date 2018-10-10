package jp.com.labit.bukuma.ui.viewholder

import android.view.ViewGroup

/**
 * Created by zoonooz on 9/15/2016 AD.
 * Base class for view holder
 * @param T object type to display
 */
abstract class BaseObjectViewHolder<in T>(parent: ViewGroup, layoutId: Int) :
    BaseViewHolder(parent, layoutId) {

  /**
   * Set the object data to specific view to display
   * @param obj object item
   */
  abstract fun setObject(obj: T)
}