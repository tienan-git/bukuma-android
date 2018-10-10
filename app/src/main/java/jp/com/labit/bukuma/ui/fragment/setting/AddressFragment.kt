package jp.com.labit.bukuma.ui.fragment.setting

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import jp.com.labit.bukuma.BukumaError
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.Address
import jp.com.labit.bukuma.ui.activity.setting.AddressEditActivity
import jp.com.labit.bukuma.ui.adapter.AddressAdapter
import jp.com.labit.bukuma.ui.adapter.BaseAdapter
import jp.com.labit.bukuma.ui.custom.SwipeItemTouchCallback
import jp.com.labit.bukuma.ui.fragment.BaseListFragment
import jp.com.labit.bukuma.ui.viewholder.AddressViewHolder
import jp.com.labit.bukuma.util.RxAlertDialog
import jp.com.labit.bukuma.util.infoDialog
import kotlinx.android.synthetic.main.fragment_list.*
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber

/**
 * Created by zoonooz on 10/3/2016 AD.
 * Address list fragment
 */
class AddressFragment : BaseListFragment<Address>() {

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

  override fun fetchStream(page: Int): Single<List<Address>> {
    return service.api.getAddresses(page).map { it.userAddresses }
  }

  override fun adapter(): BaseAdapter<Address> {
    val adapter = AddressAdapter()

    adapter.actionClick.subscribe {
      startActivityForResult(Intent(activity, AddressEditActivity::class.java), 0)
    }

    adapter.itemClick.subscribe { adr ->
      if (!adr.default) {
        RxAlertDialog.alert2(activity,
            getString(R.string.address_default_confirm_title),
            getString(R.string.address_default_confirm_message),
            getString(R.string.address_default_confirm_ok),
            getString(R.string.cancel))
            .filter { it }
            .observeOn(Schedulers.newThread())
            .flatMap {
              service.api.updateAddress(adr.id, 1,
                  adr.name, adr.postalCode, adr.prefecture, adr.country,
                  adr.city, adr.address_1, adr.address_2,
                  adr.personName, adr.personNameKana, adr.telephone).toObservable()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
              Timber.i("update default address success")
              fetch()
            }, {
              Timber.e(it, "update default address error")
            })
      }
    }

    return adapter
  }

  override fun itemTouchCallback(adapter: BaseAdapter<Address>): ItemTouchHelper.Callback? {
    val cb = SwipeItemTouchCallback(adapter, AddressViewHolder::class.java)

    cb.onItemSwiped = { adapter, item, pos, rpos ->
      if (item.default) {
        // cannot delete default address
        adapter.items.add(rpos, item)
        adapter.notifyItemInserted(pos)
        infoDialog(activity, getString(R.string.address_delete_error_default_message))
      } else {
        service.api.deleteAddress(item.id)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
              Timber.i("success delete address id : ${item.id}")
              infoDialog(activity, getString(R.string.address_delete_success_message))
              fetch()
            }, {
              Timber.e(it, "error delete address id : ${item.id}")
              adapter.items.add(rpos, item)
              adapter.notifyItemInserted(pos)

              val error = BukumaError.errorType(it)
              if (error == BukumaError.Type.HttpError && error.errorCode == 400) {
                infoDialog(activity, getString(R.string.address_delete_error_using_message))
              } else {
                infoDialog(activity, getString(R.string.error_tryagain))
              }
            })
      }
    }

    return cb
  }
}
