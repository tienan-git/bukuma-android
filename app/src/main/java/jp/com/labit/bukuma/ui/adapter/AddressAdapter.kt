package jp.com.labit.bukuma.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.Address
import jp.com.labit.bukuma.ui.viewholder.ActionAddViewHolder
import jp.com.labit.bukuma.ui.viewholder.AddressViewHolder
import jp.com.labit.bukuma.ui.viewholder.HeaderViewHolder
import rx.Observable
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 10/3/2016 AD.
 * Address adapter
 */
class AddressAdapter : BaseAdapter<Address>() {

  val TYPE_HEADER = 1
  val TYPE_ACTION = 2

  var actionClick: Observable<Void> = PublishSubject.create()
  var itemClick: Observable<Address> = PublishSubject.create()

  override fun getItemCount(): Int {
    return super.getItemCount() + 2 // header + action
  }

  override fun getItemViewType(position: Int): Int {
    return when (position) {
      0 -> TYPE_HEADER
      items.size + 1 -> TYPE_ACTION // last item
      else -> 0
    }
  }

  override fun getRealItemPosition(position: Int): Int {
    return position - 1
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      TYPE_HEADER -> HeaderViewHolder(parent)
      TYPE_ACTION -> ActionAddViewHolder(parent)
      else -> AddressViewHolder(parent)
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val context = holder.itemView.context
    if (holder is HeaderViewHolder) {
      holder.setObject(context.getString(R.string.address_header))
    } else if (holder is ActionAddViewHolder) {
      holder.setObject(context.getString(R.string.address_action_add))
      RxView.clicks(holder.itemView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        (actionClick as PublishSubject).onNext(null)
      }
    } else if (holder is AddressViewHolder) {
      val address = getItem(position)
      holder.setObject(address)
      RxView.clicks(holder.itemView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        (itemClick as PublishSubject).onNext(address)
      }
    }
  }
}
