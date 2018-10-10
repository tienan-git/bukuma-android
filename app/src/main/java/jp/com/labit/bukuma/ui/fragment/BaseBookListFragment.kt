package jp.com.labit.bukuma.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import com.jakewharton.rxbinding.view.RxView
import io.realm.Realm
import io.realm.RealmResults
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.filterLoggedIn
import jp.com.labit.bukuma.extension.toJson
import jp.com.labit.bukuma.model.Book
import jp.com.labit.bukuma.model.realm.Banner
import jp.com.labit.bukuma.ui.activity.BookActivity
import jp.com.labit.bukuma.ui.activity.WebActivity
import jp.com.labit.bukuma.ui.adapter.BaseAdapter
import jp.com.labit.bukuma.ui.adapter.BookAdapter
import jp.com.labit.bukuma.ui.viewholder.BookViewHolder
import kotlinx.android.synthetic.main.fragment_list.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber

/**
 * Created by zoonooz on 10/18/2016 AD.
 * Base book list fragment
 */
open class BaseBookListFragment : BaseListFragment<Book>() {

  lateinit var realm: Realm

  var viewingBookId: Int? = null
  var banner: Banner? = null
  var bannerIndex: Int? = null
  var banners: RealmResults<Banner>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    realm = Realm.getDefaultInstance()
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val padding = resources.getDimensionPixelSize(R.dimen.book_padding)
    recyclerview.setPadding(padding, padding, padding, padding)
    recyclerview.clipToPadding = false
  }

  override fun onResume() {
    super.onResume()
    viewingBookId?.let { refreshBookId(it) }
    viewingBookId = null
  }

  override fun onDestroyView() {
    banners?.removeChangeListeners()
    super.onDestroyView()
  }

  override fun onDestroy() {
    realm.close()
    super.onDestroy()
  }

  override fun adapter(): BaseAdapter<Book>? {

    bannerIndex?.let { index ->
      val banners = realm.where(Banner::class.java).findAllSorted("position")
      this.banners = banners

      banners.addChangeListener {
        val lastBanner = banner
        if (it.isEmpty()) {
          banner = null
        } else {
          val realm = Realm.getDefaultInstance()
          banners[index % banners.count()]?.let {
            banner = realm.copyFromRealm(it)
          }
        }

        (adapter as? BookAdapter?)?.let { adapter ->
          if (banner == null) {
            adapter.bannerUrl = null
          } else {
            banner?.let { adapter.bannerUrl = it.bannerUrl }
          }

          if (lastBanner == null && banner != null) {
            adapter.notifyItemInserted(0)
          } else if (lastBanner != null && banner == null) {
            adapter.notifyItemRemoved(0)
          } else if (lastBanner?.bannerUrl != banner?.bannerUrl) {
            adapter.notifyItemChanged(0)
          }
        }

      }

      if (banners.isNotEmpty()) {
        banners[index % banners.count()]?.let {
          banner = realm.copyFromRealm(it)
        }
      }
    }

    val adapter = BookAdapter(banner?.bannerUrl)

    adapter.bookItemClick.subscribe {
      val b = it.first
      val intent = Intent(activity, BookActivity::class.java)
      intent.putExtra(BookActivity.EXTRA_BOOK, b.toJson())
      startActivity(intent)
      viewingBookId = b.id
    }

    adapter.bannerClick.subscribe {
      banner?.let {
        val intent = Intent(activity, WebActivity::class.java)
        intent.putExtra(WebActivity.EXTRA_URL, it.url)
        startActivity(intent)
      }
    }

    adapter.likeClick.filterLoggedIn(baseActivity).subscribe {
      val book = it.first
      val position = it.second
      val vh = recyclerview.findViewHolderForLayoutPosition(position) as? BookViewHolder
      book.isLiked = !book.isLiked
      book.cachedVotesTotal += if (book.isLiked) 1 else -1
      vh?.likeView?.setLike(book.isLiked, true)
      vh?.likeTextView?.text = "${book.cachedVotesTotal}"

      val observable = if (book.isLiked) {
        service.api.likeBook(book.id)
      } else {
        service.api.unlikeBook(book.id)
      }

      // call api
      observable
          .subscribeOn(Schedulers.newThread())
          .observeOn(AndroidSchedulers.mainThread())
          .map { recyclerview.findViewHolderForLayoutPosition(position) as? BookViewHolder }
          .doOnError {
            book.isLiked = !book.isLiked
            book.cachedVotesTotal += if (book.isLiked) 1 else -1
            val h = recyclerview.findViewHolderForLayoutPosition(position) as? BookViewHolder
            h?.likeView?.setLike(book.isLiked, false)
            h?.likeTextView?.text = "${book.cachedVotesTotal}"
          }
          .subscribe({
            Timber.i("like/unlike book success")
          }, {
            Timber.e(it, "like/unlike book failed")
          })
    }

    return adapter
  }

  override fun layoutManager(): RecyclerView.LayoutManager {
    val column = resources.getInteger(R.integer.book_column)
    val layoutManager = StaggeredGridLayoutManager(column, StaggeredGridLayoutManager.VERTICAL)
    return layoutManager
  }

  fun refreshBookId(bookId: Int) {
    val adapter = recyclerview.adapter as BookAdapter
    if (adapter.items.find { it.id == bookId } != null) {
      service.api.getBook(bookId)
          .subscribeOn(Schedulers.newThread())
          .observeOn(AndroidSchedulers.mainThread())
          .takeUntil(RxView.detaches(recyclerview))
          .subscribe({
            Timber.i("refresh book success")
            it.book?.let {
              val index = adapter.items.indexOfFirst { it.id == bookId }
              if (index > -1) {
                adapter.items[index] = it
                adapter.notifyItemChanged(index)
              }
            }
          }, {
            Timber.e(it, "refresh book failed")
          })
    }
  }
}
