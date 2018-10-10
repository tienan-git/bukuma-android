package jp.com.labit.bukuma.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.model.Activity
import jp.com.labit.bukuma.ui.viewholder.ActivityViewHolder
import rx.Observable
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 11/21/2016 AD.
 * Activities adapter
 */
class ActivitiesAdapter : BaseSimpleAdapter<Activity, ActivityViewHolder>(ActivityViewHolder::class.java) {

  var userClick: Observable<Activity> = PublishSubject.create()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return ActivityViewHolder(parent)
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    super.onBindViewHolder(holder, position)

    val viewHolder = holder as ActivityViewHolder
    val item = getItem(position)

    RxView.clicks(viewHolder.avatarImageView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      (userClick as PublishSubject).onNext(item)
    }
  }
}
