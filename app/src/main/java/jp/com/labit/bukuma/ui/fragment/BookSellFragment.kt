package jp.com.labit.bukuma.ui.fragment

import android.content.Intent
import android.os.Bundle
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.toJson
import jp.com.labit.bukuma.model.Merchandise
import jp.com.labit.bukuma.ui.activity.BookActivity
import jp.com.labit.bukuma.ui.adapter.BaseAdapter
import jp.com.labit.bukuma.ui.adapter.BaseSimpleAdapter
import jp.com.labit.bukuma.ui.viewholder.MerchandiseSellingViewHolder
import rx.Single

/**
 * Created by zoonooz on 10/18/2016 AD.
 * Selling book fragment
 */
class BookSellFragment : BaseListFragment<Merchandise>() {

  override val emptyTitleText: String? get() = getString(R.string.book_selling_empty_title)
  override val emptyDescriptionText: String? get() = getString(R.string.book_selling_empty_description)
  override val emptyImageResourceId: Int get() = R.drawable.img_ph_01

  companion object {
    val ARG_USER_ID = "arg_user_id"

    fun newInstance(userId: Int? = null): BookSellFragment {
      val fragment = BookSellFragment()
      val args = Bundle()
      userId?.let { args.putInt(ARG_USER_ID, userId) }
      fragment.arguments = args
      return fragment
    }
  }

  override fun fetchStream(page: Int): Single<List<Merchandise>>? {
    val userId = arguments.getInt(ARG_USER_ID)
    return if (userId != 0) {
      service.api.getMerchandisesFromUser(userId, page).map { it.merchandises.filter { it.book != null } }
    } else {
      null
    }
  }

  override fun adapter(): BaseAdapter<Merchandise>? {
    val adapter = BaseSimpleAdapter(MerchandiseSellingViewHolder::class.java)

    adapter.itemClick.subscribe {
      it.book?.let {
        val intent = Intent(activity, BookActivity::class.java)
        intent.putExtra(BookActivity.EXTRA_BOOK, it.toJson())
        startActivity(intent)
      }
    }

    return adapter
  }
}