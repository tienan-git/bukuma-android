package jp.com.labit.bukuma.ui.fragment

import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.Book
import rx.Single

/**
 * Created by zoonooz on 9/16/2016 AD.
 * Show the list of like
 */
class BookLikedFragment : BaseBookListFragment() {

  override val emptyTitleText: String? get() = getString(R.string.book_liked_empty_title)
  override val emptyDescriptionText: String? get() = getString(R.string.book_liked_empty_description)
  override val emptyImageResourceId: Int get() = R.drawable.img_ph_06

  override fun fetchStream(page: Int): Single<List<Book>>? {
    if (service.currentUser == null) return null
    return service.api.getBooksLiked(page).map { it.books }
  }
}