package jp.com.labit.bukuma.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import io.realm.RealmResults
import io.realm.Sort
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.realm.SearchHistory
import jp.com.labit.bukuma.ui.viewholder.ActionViewHolder
import jp.com.labit.bukuma.ui.viewholder.HeaderViewHolder
import jp.com.labit.bukuma.ui.viewholder.SearchHistoryViewHolder
import jp.com.labit.bukuma.ui.viewholder.SearchResultViewHolder
import rx.Observable
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit


/**
 * Created by zoonooz on 9/13/2016 AD.
 * Search adapter
 */
class SearchAdapter : CategoryAdapter(1) {

  companion object {
    /**
     * show all categories and recent search
     */
    val MODE_ALL = 0

    /**
     * show only recent search and delete history button
     */
    val MODE_RECENT = 1

    /**
     * show result title list
     */
    val MODE_RESULT = 2
  }

  // handle functions
  var resultClick: Observable<String> = PublishSubject.create()
  var historyClick: Observable<SearchHistory> = PublishSubject.create()
  var clearHistoryClick: Observable<Void> = PublishSubject.create()

  val TYPE_CATEGORY = 0
  val TYPE_HEADER = 1
  val TYPE_RECENT = 2
  val TYPE_CLEAR_RECENT = 3
  val TYPE_RESULT = 4

  /**
   * content mode
   */
  var mode = MODE_ALL

  var histories: RealmResults<SearchHistory>? = null
  var result = emptyList<String>()

  init {
    showColor = true
  }

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
    super.onAttachedToRecyclerView(recyclerView)
    histories = realm.where(SearchHistory::class.java).findAllSorted("time", Sort.DESCENDING)
    histories?.addChangeListener { notifyDataSetChanged() }
  }

  override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
    super.onDetachedFromRecyclerView(recyclerView)
    histories?.removeChangeListeners()
    histories = null
  }

  override fun getItemCount(): Int {
    var count = 0
    val historyCount = histories?.size ?: 0
    if (historyCount > 0) count += historyCount

    if (mode == MODE_ALL) {
      count += super.getItemCount()
      if (histories?.isNotEmpty() ?: false) count += 1 // 1 for header
    } else if (mode == MODE_RESULT) {
      count = result.size
    } else {
      // mode recent
      count += 1 // clear history
    }
    return count
  }

  override fun getItemViewType(position: Int): Int {
    return if (mode == MODE_ALL) {
      if (position < items.size) {
        TYPE_CATEGORY
      } else if (position == items.size) {
        TYPE_HEADER
      } else {
        TYPE_RECENT
      }
    } else if (mode == MODE_RESULT) {
      TYPE_RESULT
    } else {
      if (position < histories?.size ?: 0) {
        TYPE_RECENT
      } else {
        TYPE_CLEAR_RECENT
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      TYPE_HEADER -> HeaderViewHolder(parent)
      TYPE_RECENT -> SearchHistoryViewHolder(parent)
      TYPE_CLEAR_RECENT -> ActionViewHolder(parent)
      TYPE_RESULT -> SearchResultViewHolder(parent)
      else -> super.onCreateViewHolder(parent, viewType)
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    super.onBindViewHolder(holder, position)

    if (holder is HeaderViewHolder) {
      holder.setObject(holder.itemView.context.getString(R.string.search_history_title))
    } else if (holder is SearchHistoryViewHolder) {
      val pos = if (mode == MODE_ALL) position - (items.size + 1) else position
      val history = histories!![pos]
      holder.setText("${history.text}")
      RxView.clicks(holder.itemView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        (historyClick as PublishSubject).onNext(history)
      }
    } else if (holder is ActionViewHolder) {
      holder.setObject(holder.itemView.context.getString(R.string.search_history_clear))
      RxView.clicks(holder.itemView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        (clearHistoryClick as PublishSubject).onNext(null)
      }
    } else if (holder is SearchResultViewHolder) {
      val word = result[position]
      holder.setText(word)
      RxView.clicks(holder.itemView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        (resultClick as PublishSubject).onNext(word)
      }
    }
  }
}
