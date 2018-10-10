package jp.com.labit.bukuma.ui.adapter

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.model.Book
import jp.com.labit.bukuma.ui.viewholder.BannerViewHolder
import jp.com.labit.bukuma.ui.viewholder.BookViewHolder
import rx.Observable
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 10/14/2016 AD.
 * Book adapter
 */
class BookAdapter(var bannerUrl: String? = null) : BaseAdapter<Book>() {

  val TYPE_BANNER = 1
  val TYPE_ITEM = 0

  override var animate = true

  var itemClick: Observable<Book> = PublishSubject.create()
  var bookItemClick: Observable<Pair<Book, BookViewHolder>> = PublishSubject.create()
  var likeClick: Observable<Pair<Book, Int>> = PublishSubject.create()
  var bannerClick: Observable<String> = PublishSubject.create()

  override fun getItemCount(): Int {
    return super.getItemCount() + if (!bannerUrl.isNullOrEmpty()) 1 else 0
  }

  override fun getRealItemPosition(position: Int): Int {
    return super.getRealItemPosition(position) - if (!bannerUrl.isNullOrEmpty()) 1 else 0
  }

  override fun getItemViewType(position: Int): Int {
    return if (!bannerUrl.isNullOrEmpty() && position == 0) TYPE_BANNER
    else TYPE_ITEM
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return if (viewType == TYPE_BANNER) BannerViewHolder(parent)
    else BookViewHolder(parent)
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    super.onBindViewHolder(holder, position)
    if (holder is BookViewHolder) {
      val book = getItem(position)
      holder.setObject(book)

      RxView.clicks(holder.itemView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        (itemClick as PublishSubject).onNext(book)
      }

      RxView.clicks(holder.likeView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        (likeClick as PublishSubject).onNext(Pair(book, position))
      }

      RxView.clicks(holder.itemView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        (bookItemClick as PublishSubject).onNext(Pair(book, holder))
      }
    } else if (holder is BannerViewHolder) {
      bannerUrl?.let { holder.setObject(it) }
      (holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
      RxView.clicks(holder.itemView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        (bannerClick as PublishSubject).onNext(bannerUrl)
      }
    }
  }
}
