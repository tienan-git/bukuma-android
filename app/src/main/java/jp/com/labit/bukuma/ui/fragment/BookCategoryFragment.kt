package jp.com.labit.bukuma.ui.fragment

import android.os.Bundle
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.Book
import jp.com.labit.bukuma.model.realm.Category
import kotlinx.android.synthetic.main.fragment_list.*
import rx.Single

/**
 * Created by zoonooz on 10/13/2016 AD.
 * Book list fragment
 */
class BookCategoryFragment : BaseBookListFragment() {

  override val emptyTitleText: String? get() = getString(R.string.search_empty_title)
  override val emptyDescriptionText: String? get() = getString(R.string.search_empty_description)
  override val emptyImageResourceId: Int get() = R.drawable.img_ph_01

  var categoryId = 0
  var includeChild = false

  companion object {
    val ARG_CATEGORY_ID = "arg_category_id"
    val ARG_INCLUDE_CHILD = "arg_include_child"
    val ARG_BANNER_INDEX = "arg_banner_index"

    fun newInstance(
        categoryId: Int,
        includeChild: Boolean = false,
        bannerIndex: Int? = null): BookCategoryFragment {
      val fragment = BookCategoryFragment()
      val args = Bundle()
      args.putInt(ARG_CATEGORY_ID, categoryId)
      args.putBoolean(ARG_INCLUDE_CHILD, includeChild)
      bannerIndex?.let { args.putInt(ARG_BANNER_INDEX, it) }
      fragment.arguments = args
      return fragment
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    categoryId = arguments.getInt(ARG_CATEGORY_ID, 0)
    if (arguments.containsKey(ARG_BANNER_INDEX)) {
      bannerIndex = arguments.getInt(ARG_BANNER_INDEX)
    }
    super.onCreate(savedInstanceState)

    if (categoryId == 0) {
      throw IllegalStateException("category id should not be zero")
    }
  }

  override fun fetchStream(page: Int): Single<List<Book>>? {
    val obs = if (categoryId == Category.ID_ALL) {
      service.api.getBooksTimeline(1, page)
    } else if (categoryId == Category.ID_UNDER300) {
      service.api.getBooksUnderPrice(1, 300, page)
    } else if (categoryId == Category.ID_PUBLISH6M) {
      service.api.getBooksPublishedInDay(1, 180, page)
    } else if (categoryId == Category.ID_BULK) {
      service.api.getBooksBulk(1, page)
    } else if (includeChild) {
      service.api.getBooksTimeline(categoryId, page)
    } else if (categoryId == Category.ID_HUMANITY) {
      service.api.getBooksTimeline(Category.ID_HUMANITY, page)
    } else if (categoryId == Category.ID_NOVEL) {
      service.api.getBooksTimeline(Category.ID_NOVEL, page)
    } else if (categoryId == Category.ID_SOCIETY) {
      service.api.getBooksTimeline(Category.ID_SOCIETY, page)
    } else if (categoryId == Category.ID_NONFICTION) {
      service.api.getBooksTimeline(Category.ID_NONFICTION, page)
    } else if (categoryId == Category.ID_HISTORY) {
      service.api.getBooksTimeline(Category.ID_HISTORY, page)
    } else if (categoryId == Category.ID_BUSINESS) {
      service.api.getBooksTimeline(Category.ID_BUSINESS, page)
    } else if (categoryId == Category.ID_INVESTMENT) {
      service.api.getBooksTimeline(Category.ID_INVESTMENT, page)
    } else if (categoryId == Category.ID_IT) {
      service.api.getBooksTimeline(Category.ID_IT, page)
    } else if (categoryId == Category.ID_BEAUTY) {
      service.api.getBooksTimeline(Category.ID_BEAUTY, page)
    } else if (categoryId == Category.ID_HOBBY) {
      service.api.getBooksTimeline(Category.ID_HOBBY, page)
    } else if (categoryId == Category.ID_MAGAZINE) {
      service.api.getBooksTimeline(Category.ID_MAGAZINE, page)
    } else if (categoryId == Category.ID_COMIC) {
      service.api.getBooksTimeline(Category.ID_COMIC, page)
    } else if (categoryId == Category.ID_PICTURE) {
      service.api.getBooksTimeline(Category.ID_PICTURE, page)
    } else if (categoryId == Category.ID_LANGUAGE) {
      service.api.getBooksTimeline(Category.ID_LANGUAGE, page)
    } else if (categoryId == Category.ID_CAPABILITY) {
      service.api.getBooksTimeline(Category.ID_CAPABILITY, page)
    } else if (categoryId == Category.ID_EDUCATION) {
      service.api.getBooksTimeline(Category.ID_EDUCATION, page)
    } else if (categoryId == Category.ID_SCIENCE) {
      service.api.getBooksTimeline(Category.ID_SCIENCE, page)
    } else if (categoryId == Category.ID_MEDICINE) {
      service.api.getBooksTimeline(Category.ID_MEDICINE, page)
    } else if (categoryId == Category.ID_SPORTS) {
      service.api.getBooksTimeline(Category.ID_SPORTS, page)
    } else if (categoryId == Category.ID_JOURNEY) {
      service.api.getBooksTimeline(Category.ID_JOURNEY, page)
    } else if (categoryId == Category.ID_BUILDING) {
      service.api.getBooksTimeline(Category.ID_BUILDING, page)
    } else if (categoryId == Category.ID_SCORE) {
      service.api.getBooksTimeline(Category.ID_SCORE, page)
    } else if (categoryId == Category.ID_ENTERTAINMENT) {
      service.api.getBooksTimeline(Category.ID_ENTERTAINMENT, page)
    } else if (categoryId == Category.ID_LIGHTNOVEL) {
      service.api.getBooksTimeline(Category.ID_LIGHTNOVEL, page)
    } else if (categoryId == Category.ID_FOREIGN) {
      service.api.getBooksTimeline(Category.ID_FOREIGN, page)
    } else if (categoryId == Category.ID_TALENT) {
      service.api.getBooksTimeline(Category.ID_TALENT, page)
    } else {
      service.api.getBookFromCategory(categoryId, page)
    }
    return obs.map { it.books }
  }

  override fun hideEmptyView() {
    super.hideEmptyView()
    recyclerview.setBackgroundResource(android.R.color.transparent)
  }
}
