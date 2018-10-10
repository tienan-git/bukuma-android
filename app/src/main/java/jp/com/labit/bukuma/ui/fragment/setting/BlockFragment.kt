package jp.com.labit.bukuma.ui.fragment.setting

import android.app.ProgressDialog
import android.support.v7.widget.helper.ItemTouchHelper
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.BlockUser
import jp.com.labit.bukuma.ui.adapter.BaseAdapter
import jp.com.labit.bukuma.ui.adapter.BaseSimpleAdapter
import jp.com.labit.bukuma.ui.custom.SwipeItemTouchCallback
import jp.com.labit.bukuma.ui.fragment.BaseListFragment
import jp.com.labit.bukuma.ui.viewholder.BlockViewHolder
import jp.com.labit.bukuma.util.RxAlertDialog
import jp.com.labit.bukuma.util.infoDialog
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber

/**
 * Created by zoonooz on 10/3/2016 AD.
 * Block users list fragment
 */
class BlockFragment : BaseListFragment<BlockUser>() {

  override val emptyTitleText: String? get() = getString(R.string.block_empty_title)
  override val emptyDescriptionText: String? get() = getString(R.string.block_empty_description)
  override val emptyImageResourceId: Int get() = R.drawable.img_ph_01

  override fun fetchStream(page: Int): Single<List<BlockUser>> {
    return service.api.getBlockedUsers(page).map { it.blockUsers }
  }

  override fun adapter(): BaseAdapter<BlockUser> {
    val adapter = BaseSimpleAdapter(BlockViewHolder::class.java)
    return adapter
  }

  override fun itemTouchCallback(adapter: BaseAdapter<BlockUser>): ItemTouchHelper.Callback? {
    val cb = SwipeItemTouchCallback(adapter)

    cb.onItemSwiped = { adapter, item, pos, rpos ->
      val progress = ProgressDialog(activity)
      progress.setMessage(getString(R.string.loading))
      progress.setCancelable(false)
      RxAlertDialog.alert2(activity, null,
          getString(R.string.block_unblock_confirm_message),
          getString(R.string.block_unblock_confirm_ok),
          getString(R.string.cancel))
          .doOnNext {
            if (!it) { // cancel the swipe
              adapter.items.add(pos, item)
              adapter.notifyItemInserted(pos)
            }
          }
          .filter { it }
          .doOnNext { progress.show() }
          .observeOn(Schedulers.newThread())
          .flatMap { service.api.unblockUser(item.target!!.id).toObservable() }
          .observeOn(AndroidSchedulers.mainThread())
          .doOnEach { progress.dismiss() }
          .subscribe({
            Timber.i("success unblock user id : ${item.target!!.id}")
            infoDialog(activity, getString(R.string.block_unblock_success_message))
            adapter.notifyDataSetChanged()
          }, {
            Timber.e(it, "error unblock user id : ${item.target!!.id}")
            adapter.items.add(pos, item)
            adapter.notifyItemInserted(pos)
            infoDialog(activity, getString(R.string.error_tryagain))
          })
    }

    return cb
  }
}