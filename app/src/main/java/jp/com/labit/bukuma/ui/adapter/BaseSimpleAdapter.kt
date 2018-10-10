package jp.com.labit.bukuma.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.ui.viewholder.BaseObjectViewHolder
import rx.Observable
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 9/15/2016 AD.
 * Base one type simple adapter. This will basically handle necessary logic to display items
 * without subclassing adapter.
 *
 * @param T item object type
 * @param E view holder class that will be used to display item
 */
open class BaseSimpleAdapter<T, E : BaseObjectViewHolder<T>>(private val clazz: Class<E>) : BaseAdapter<T>() {

  /** Observable for item click event. Subscribe this to handle */
  var itemClick: Observable<T> = PublishSubject.create()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return clazz.getConstructor(ViewGroup::class.java).newInstance(parent)
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    super.onBindViewHolder(holder, position)
    if (clazz.isInstance(holder)) {
      val obj = getItem(position)
      @Suppress("UNCHECKED_CAST")
      (holder as E).setObject(obj)
      RxView.clicks(holder.itemView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        (itemClick as PublishSubject).onNext(obj)
      }
    }
  }
}
