package jp.com.labit.bukuma.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.api.BukumaService
import jp.com.labit.bukuma.model.Merchandise
import jp.com.labit.bukuma.model.Review
import jp.com.labit.bukuma.model.realm.User
import jp.com.labit.bukuma.ui.viewholder.*
import rx.Observable
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 9/28/2016 AD.
 * Profile list adapter
 */
class ProfileAdapter(var user: User, val service: BukumaService, val isSelf: Boolean) : BaseAdapter<Review>() {

  val avatarClick: Observable<Void> = PublishSubject.create()
  val actionClick: Observable<Void> = PublishSubject.create()
  val reviewClick: Observable<Review> = PublishSubject.create()
  val bookClick: Observable<Merchandise> = PublishSubject.create()
  val bookHeaderClick: Observable<Void> = PublishSubject.create()

  val TYPE_INFO = 4
  val TYPE_BOOK = 3
  val TYPE_BOOK_HEADER = 2
  val TYPE_REVIEW_HEADER = 1
  val TYPE_REVIEW_ITEM = 0

  override fun getItemViewType(position: Int): Int {
    val hasBook = user.merchandisesCount > 0
    return if (position == 0) {
      TYPE_INFO
    } else if (position == 1) {
      TYPE_BOOK_HEADER
    } else if (position == 2) {
      if (hasBook) TYPE_BOOK else TYPE_REVIEW_HEADER
    } else if (position == 3 && hasBook) {
      TYPE_REVIEW_HEADER
    } else {
      TYPE_REVIEW_ITEM
    }
  }

  override fun getItemCount(): Int {
    var count = super.getItemCount()
    count += 3 // user info, book header, review header
    if (user.merchandisesCount > 0) count += 1 // book list
    return count
  }

  override fun getRealItemPosition(position: Int): Int {
    return position - if (user.merchandisesCount > 0) 4 else 3
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      TYPE_INFO -> UserInfoViewHolder(parent, isSelf)
      TYPE_BOOK_HEADER -> DisclosureViewHolder(parent)
      TYPE_BOOK -> ProfileBookViewHolder(parent, user.id, service).apply {
        adapter.itemClick.throttleFirst(1, TimeUnit.SECONDS).subscribe {
          (bookClick as PublishSubject).onNext(it)
        }
      }
      TYPE_REVIEW_HEADER -> ReviewHeaderViewHolder(parent)
      else -> ReviewViewHolder(parent)
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val context = holder.itemView.context
    if (holder is UserInfoViewHolder) {
      holder.setObject(user)
      RxView.clicks(holder.actionButton).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        (actionClick as PublishSubject).onNext(null)
      }
      RxView.clicks(holder.avatarImageView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        (avatarClick as PublishSubject).onNext(null)
      }
    } else if (holder is DisclosureViewHolder) {

      if(user.merchandisesCount > 1000) {
        holder.setObject(context.getString(R.string.profile_book_header_thousand))
      } else {
        holder.setObject(context.getString(R.string.profile_book_header, user.merchandisesCount))
      }

      RxView.clicks(holder.itemView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        (bookHeaderClick as PublishSubject).onNext(null)
      }
    } else if (holder is ProfileBookViewHolder) {
      holder.load()
    } else if (holder is ReviewHeaderViewHolder) {
      holder.setObject(user)
    } else if (holder is ReviewViewHolder) {
      val review = getItem(position)
      holder.setObject(review)
      RxView.clicks(holder.itemView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        (reviewClick as PublishSubject).onNext(review)
      }
    }
  }
}
