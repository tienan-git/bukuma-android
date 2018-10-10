package jp.com.labit.bukuma.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.Transaction
import jp.com.labit.bukuma.ui.viewholder.HeaderViewHolder
import jp.com.labit.bukuma.ui.viewholder.TodoViewHolder
import rx.Observable
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 10/24/2016 AD.
 * Transaction adapter
 */
class TodoAdapter(val userId: Int) : BaseAdapter<Transaction>() {

  val TYPE_CONTENT = 0
  val TYPE_HEADER_SELF = 1
  val TYPE_HEADER_PARTNER = 2

  var userClick: Observable<Transaction> = PublishSubject.create()
  var itemClick: Observable<Transaction> = PublishSubject.create()

  var todoCount: Int = 0
  var isNoTodo = false
  var isNoWaiting = false

  override fun getItemCount(): Int {
    if (isNoTodo && isNoWaiting) return super.getItemCount()
    if (isNoTodo || isNoWaiting) {
      return super.getItemCount() + 1
    }
    return super.getItemCount() + 2
  }

  override fun getItemViewType(position: Int): Int {
    if (isNoTodo && isNoWaiting) return super.getItemViewType(position)
    if (isNoTodo) {
      return when(position) {
        0 -> TYPE_HEADER_PARTNER
        else -> TYPE_CONTENT
      }
    } else if (isNoWaiting) {
      return when(position) {
        0 -> TYPE_HEADER_SELF
        else -> TYPE_CONTENT
      }
    }
    return when(position) {
      0 -> TYPE_HEADER_SELF
      todoCount + 1 -> TYPE_HEADER_PARTNER
      else -> TYPE_CONTENT
    }
  }

  override fun getRealItemPosition(position: Int): Int {
    if (isNoTodo && isNoWaiting) return super.getRealItemPosition(position)
    if (isNoTodo || isNoWaiting) {
      return position - 1
    }
    return if (position > todoCount + 1) {
      position - 2
    } else {
      position - 1
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when(viewType) {
      TYPE_HEADER_SELF, TYPE_HEADER_PARTNER -> HeaderViewHolder(parent)
      else -> TodoViewHolder(parent, userId)
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val context = holder.itemView.context
    if (holder is HeaderViewHolder) {
      if (isNoTodo) {
        if (position == 0) holder.setObject(context.getString(R.string.todo_text_header_partner))
      } else if (isNoWaiting) {
        if (position == 0) holder.setObject(context.getString(R.string.todo_text_header_self))
      } else if (!isNoTodo && !isNoWaiting) {
        if (position == 0) {
          holder.setObject(context.getString(R.string.todo_text_header_self))
        } else if (position == todoCount + 1) {
          holder.setObject(context.getString(R.string.todo_text_header_partner))
        }
      }
    } else if (holder is TodoViewHolder) {
      val item = getItem(position)
      holder.setObject(item)
      RxView.clicks(holder.avatarImageView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        (userClick as PublishSubject).onNext(item)
      }
      RxView.clicks(holder.itemView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        (itemClick as PublishSubject).onNext(item)
      }
    }
  }
}