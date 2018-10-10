package jp.com.labit.bukuma.ui.fragment.setting

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.CreditCard
import jp.com.labit.bukuma.ui.activity.setting.CreditCardEditActivity
import jp.com.labit.bukuma.ui.adapter.BaseAdapter
import jp.com.labit.bukuma.ui.adapter.CreditCardAdapter
import jp.com.labit.bukuma.ui.custom.SwipeItemTouchCallback
import jp.com.labit.bukuma.ui.fragment.BaseListFragment
import jp.com.labit.bukuma.ui.viewholder.CreditCardViewHolder
import jp.com.labit.bukuma.util.RxAlertDialog
import jp.com.labit.bukuma.util.infoDialog
import kotlinx.android.synthetic.main.fragment_list.*
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber

/**
 * Created by zoonooz on 10/5/2016 AD.
 * Credit card list fragment
 */
class CreditCardFragment : BaseListFragment<CreditCard>() {

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    refresh_layout.isEnabled = false
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
      fetch() // refresh data if its update
    }
  }

  override fun fetchStream(page: Int): Single<List<CreditCard>>? {
    return service.api.getCreditCards(page).map { it.userPaymentSources }
  }

  override fun adapter(): BaseAdapter<CreditCard>? {
    val adapter = CreditCardAdapter()

    adapter.actionClick.subscribe {
      startActivityForResult(Intent(activity, CreditCardEditActivity::class.java), 0)
    }

    adapter.itemClick.subscribe { card ->
      if (!card.default) {
        RxAlertDialog.alert2(activity,
            getString(R.string.payment_creditcard_default_confirm_title),
            getString(R.string.payment_creditcard_default_confirm_message),
            getString(R.string.payment_creditcard_default_confirm_ok),
            getString(R.string.cancel))
            .filter { it }
            .observeOn(Schedulers.newThread())
            .flatMap { service.api.setCreditCardActive(card.id, 1).toObservable() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
              Timber.i("update default card success")
              fetch()
            }, {
              Timber.e(it, "update default card error")
            })
      }
    }

    return adapter
  }

  override fun itemTouchCallback(adapter: BaseAdapter<CreditCard>): ItemTouchHelper.Callback? {
    val cb = SwipeItemTouchCallback(adapter, CreditCardViewHolder::class.java)

    cb.onItemSwiped = { adapter, item, pos, rpos ->
      if (item.default && item.info?.securityCodeCheck ?: false) {
        // cannot delete default credit card + security code valid
        adapter.items.add(rpos, item)
        adapter.notifyItemInserted(pos)
        infoDialog(activity, getString(R.string.payment_creditcard_delete_error_default_message))
      } else {
        service.api.deleteCreditCard(item.id)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
              Timber.i("success delete card id : ${item.id}")
              infoDialog(activity, getString(R.string.payment_creditcard_delete_success_message))
            }, {
              Timber.e(it, "error delete card id : ${item.id}")
              adapter.items.add(rpos, item)
              adapter.notifyItemInserted(pos)
            })
      }
    }

    return cb
  }
}