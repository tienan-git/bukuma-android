package jp.com.labit.bukuma.ui.fragment

import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.Book
import rx.Single

/**
 * Created by zoonooz on 10/18/2016 AD.
 * Bought book list
 */
class BookBoughtFragment : BaseBookListFragment() {

  override val emptyTitleText: String? get() = getString(R.string.book_bought_empty_title)
  override val emptyDescriptionText: String? get() = getString(R.string.book_bought_empty_description)
  override val emptyImageResourceId: Int get() = R.drawable.img_ph_05

  override fun fetchStream(page: Int): Single<List<Book>>? {
    if (service.currentUser == null) return null
    return service.api.getMyTransactions(page)
        .map { it.itemTransactions
            .filter { it.merchandise?.book != null }
            .map { it.merchandise!!.book!! }}
  }
}