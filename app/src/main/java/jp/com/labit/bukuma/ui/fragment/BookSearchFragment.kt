package jp.com.labit.bukuma.ui.fragment

import android.os.Bundle
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.api.BukumaApi
import jp.com.labit.bukuma.model.Book
import jp.com.labit.bukuma.ui.dialog.SearchingDialog
import kotlinx.android.synthetic.main.activity_chat.*
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber

/**
 * Created by zoonooz on 10/19/2016 AD.
 * Book search result fragment
 */
class BookSearchFragment : BaseBookListFragment() {

  override val emptyTitleText: String? get() = getString(R.string.search_empty_title)
  override val emptyDescriptionText: String? get() = getString(R.string.search_empty_description)
  override val emptyImageResourceId: Int get() = R.drawable.img_ph_01

  var keyword = ""
  var orderEnable = false

  private var orderSearchStarted = false

  companion object {
    val ARG_KEYWORD = "arg_keyword"
    val ARG_ORDER_ENABLE = ""

    fun newInstance(keyword: String, orderEnable: Boolean): BookSearchFragment {
      val fragment = BookSearchFragment()
      val args = Bundle()
      args.putString(ARG_KEYWORD, keyword)
      args.putBoolean(ARG_ORDER_ENABLE, orderEnable)
      fragment.arguments = args
      return fragment
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    keyword = arguments.getString(ARG_KEYWORD, "")
    orderEnable = arguments.getBoolean(ARG_ORDER_ENABLE)
  }

  override fun fetchStream(page: Int): Single<List<Book>>? {
    return service.api.searchBooks(keyword, page)
        .map { it.books }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess { tracker.trackBookSearchTitle(keyword) }
        .doOnSuccess {
          if (page == BukumaApi.START_PAGE && it.isEmpty() && orderEnable) beginSearchOrder()
        }
  }

  fun beginSearchOrder() {
    if (!orderSearchStarted) {
      orderSearchStarted = true

      // testing
      val dialog = SearchingDialog()
      dialog.isCancelable = false
      dialog.show(fragmentManager, "search")

      service.books.searchOrder(keyword)
          .takeUntil(RxView.detaches(recyclerview))
          .doOnEach { dialog.dismiss() }
          .subscribe({
            Timber.i("search order finish")
            fetch()
          }, {
            Timber.e(it, "search order error")
          })
    }
  }
}
