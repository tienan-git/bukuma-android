package jp.com.labit.bukuma.ui.fragment

import android.os.Bundle
import jp.com.labit.bukuma.model.Merchandise
import jp.com.labit.bukuma.ui.adapter.BaseAdapter
import jp.com.labit.bukuma.ui.adapter.BaseSimpleAdapter
import jp.com.labit.bukuma.ui.viewholder.MerchandiseChooserViewHolder
import rx.Single

/**
 * Created by zoonooz on 11/2/2016 AD.
 * Merchandise list
 */
class MerchandiseChooserFragment : BaseListFragment<Merchandise>() {

  var userId = 0

  companion object {
    val ARG_USER_ID = "arg_user_id"

    fun newInstance(fromUserId: Int? = null): MerchandiseChooserFragment {
      val fragment = MerchandiseChooserFragment()
      val args = Bundle()
      fromUserId?.let { args.putInt(ARG_USER_ID, it) }
      fragment.arguments = args
      return fragment
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    userId = arguments.getInt(ARG_USER_ID)
    if (userId == 0) {
      userId = service.currentUser!!.id
    }
  }

  override fun adapter(): BaseAdapter<Merchandise>? {
    val adapter = BaseSimpleAdapter(MerchandiseChooserViewHolder::class.java)

    adapter.itemClick.subscribe {
      (activity as? Callback)?.onMerchandiseSelect(it)
    }

    return adapter
  }

  override fun fetchStream(page: Int): Single<List<Merchandise>>? {
    return service.api.getMerchandisesFromUser(userId, page).map { it.merchandises }
  }

  interface Callback {
    fun onMerchandiseSelect(merchandise: Merchandise)
  }
}
