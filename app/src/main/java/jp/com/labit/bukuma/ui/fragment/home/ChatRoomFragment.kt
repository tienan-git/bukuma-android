package jp.com.labit.bukuma.ui.fragment.home

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.com.labit.bukuma.BukumaApplication
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.api.BukumaApi
import jp.com.labit.bukuma.extension.toJson
import jp.com.labit.bukuma.fcm.NotificationCreator
import jp.com.labit.bukuma.model.realm.ChatRoom
import jp.com.labit.bukuma.ui.activity.ChatActivity
import jp.com.labit.bukuma.ui.activity.ProfileActivity
import jp.com.labit.bukuma.ui.adapter.BaseAdapter
import jp.com.labit.bukuma.ui.adapter.ChatRoomAdapter
import jp.com.labit.bukuma.ui.custom.SwipeItemTouchCallback
import jp.com.labit.bukuma.ui.fragment.BaseListFragment
import jp.com.labit.bukuma.ui.viewholder.ChatRoomViewHolder
import jp.com.labit.bukuma.util.RxAlertDialog
import jp.com.labit.bukuma.util.infoDialog
import kotlinx.android.synthetic.main.activity_chat.*
import rx.Single
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber

/**
 * Created by zoonooz on 10/6/2016 AD.
 * Chatroom list fragment
 */
class ChatRoomFragment : BaseListFragment<ChatRoom>() {

  override val emptyTitleText: String? get() = getString(R.string.chatroom_empty_title)
  override val emptyImageResourceId: Int get() = R.drawable.img_ph_01
  override val fetchWhenResume = true

  private var notiSubscription: Subscription? = null

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    notiSubscription = (activity.application as BukumaApplication)
        .notificationObservable
        .observeOn(AndroidSchedulers.mainThread())
        .filter { it.type == NotificationCreator.Type.MESSAGE }
        .subscribe { fetch() }
    return super.onCreateView(inflater, container, savedInstanceState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    notiSubscription?.unsubscribe()
  }

  override fun fetchStream(page: Int): Single<List<ChatRoom>>? {
    val fromTime = ((recyclerview.adapter as? BaseAdapter<*>)?.items?.lastOrNull() as? ChatRoom)?.updatedAt
    return service.chatRooms.getChatRooms(if (page == BukumaApi.START_PAGE) null else fromTime)
  }

  override fun adapter(): BaseAdapter<ChatRoom>? {
    val adapter = ChatRoomAdapter()

    adapter.userClick.subscribe {
      it.friend()?.let {
        val intent = Intent(activity, ProfileActivity::class.java)
        intent.putExtra(ProfileActivity.EXTRA_USER, it.toJson())
        startActivity(intent)
      }
    }

    adapter.itemClick.subscribe {
      val intent = Intent(activity, ChatActivity::class.java)
      intent.putExtra(ChatActivity.EXTRA_ROOM_ID, it.id)
      startActivity(intent)
    }

    return adapter
  }

  override fun itemTouchCallback(adapter: BaseAdapter<ChatRoom>): ItemTouchHelper.Callback? {
    val cb = SwipeItemTouchCallback(adapter, ChatRoomViewHolder::class.java)

    cb.onItemSwiped = { adapter, item, pos, rpos ->
      RxAlertDialog.alert2(activity, null,
          getString(R.string.chatroom_delete_confirm_message),
          getString(R.string.chatroom_delete_confirm_ok),
          getString(R.string.cancel))
          .doOnNext {
            if (!it) {
              // cannot delete default credit card + security code valid
              adapter.items.add(rpos, item)
              adapter.notifyItemInserted(pos)
            }
          }
          .filter { it }
          .observeOn(Schedulers.newThread())
          .flatMap { service.chatRooms.deleteChatRoom(item.id).toObservable() }
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe({
            Timber.i("delete room success")
            adapter.notifyDataSetChanged()
          }, {
            Timber.e(it, "delete room error")
            adapter.items.add(pos, item)
            adapter.notifyItemInserted(pos)
            infoDialog(activity, getString(R.string.error_tryagain))
          })
    }

    return cb
  }
}
