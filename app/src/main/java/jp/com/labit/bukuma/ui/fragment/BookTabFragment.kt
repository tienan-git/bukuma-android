package jp.com.labit.bukuma.ui.fragment

import android.os.Bundle
import io.realm.Realm
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.Book
import jp.com.labit.bukuma.model.realm.Tab
import kotlinx.android.synthetic.main.activity_chat.*
import rx.Single

/**
 * Created by tani on 2017/05/11.
 */
class BookTabFragment : BaseBookListFragment() {

  override val emptyTitleText: String? get() = getString(R.string.search_empty_title)
  override val emptyDescriptionText: String? get() = getString(R.string.search_empty_description)
  override val emptyImageResourceId: Int get() = R.drawable.img_ph_01

  var tabId = 0

  companion object {
    val ARG_TAB_ID = "arg_tab_id"
    val ARG_BANNER_INDEX = "arg_banner_index"

    fun newInstance(
        tabId: Int,
        bannerIndex: Int? = null): BookTabFragment {
      val fragment = BookTabFragment()
      val args = Bundle()
      args.putInt(ARG_TAB_ID, tabId)
      bannerIndex?.let { args.putInt(ARG_BANNER_INDEX, it) }
      fragment.arguments = args
      return fragment
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    tabId = arguments.getInt(ARG_TAB_ID, 0)
    if (arguments.containsKey(ARG_BANNER_INDEX)) {
      bannerIndex = arguments.getInt(ARG_BANNER_INDEX)
    }

    super.onCreate(savedInstanceState)

    if (tabId == 0) {
      throw IllegalStateException("tab id should not be zero")
    }
  }

  override fun fetchStream(page: Int): Single<List<Book>>? {
    return Realm.getDefaultInstance()
        .where(Tab::class.java)
        .equalTo("id", tabId)
        .findFirst()?.let {
          service.api.getBooks(it.url, page).map { it.books }
        }
  }

  override fun hideEmptyView() {
    super.hideEmptyView()
    recyclerview.setBackgroundResource(android.R.color.transparent)
  }
}
