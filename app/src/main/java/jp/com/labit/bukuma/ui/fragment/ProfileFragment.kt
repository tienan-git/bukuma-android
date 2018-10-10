package jp.com.labit.bukuma.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import jp.com.labit.bukuma.extension.applyIfVerified
import jp.com.labit.bukuma.extension.filterLoggedIn
import jp.com.labit.bukuma.extension.toJson
import jp.com.labit.bukuma.extension.toObject
import jp.com.labit.bukuma.model.Review
import jp.com.labit.bukuma.model.realm.User
import jp.com.labit.bukuma.ui.activity.*
import jp.com.labit.bukuma.ui.activity.drawer.SellingActivity
import jp.com.labit.bukuma.ui.adapter.BaseAdapter
import jp.com.labit.bukuma.ui.adapter.ProfileAdapter
import jp.com.labit.bukuma.util.dpToPx
import kotlinx.android.synthetic.main.fragment_list.*
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber

/**
 * Created by zoonooz on 9/28/2016 AD.
 * Fragment to show user profile
 */
class ProfileFragment : BaseListFragment<Review>() {

  interface ProfileFragmentCallback {
    fun onHideTopArea()
    fun onShowTopArea()
  }

  companion object {
    val ARG_USER = "arg_user"
    fun newInstance(user: User? = null): ProfileFragment {
      val fragment = ProfileFragment()
      val args = Bundle()
      user?.let { args.putString(ARG_USER, user.toJson()) }
      fragment.arguments = args
      return fragment
    }
  }

  private var showingTopArea = true
  var user: User? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    user = user()
  }

  override fun onResume() {
    super.onResume()
    // refresh user
    user()?.let { u ->
      (recyclerview.adapter as ProfileAdapter).let {
        it.user = u
        it.notifyItemChanged(0)
      }
    }
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val point = dpToPx(resources, 70f)
        val offset = recyclerView.computeVerticalScrollOffset()
        if (showingTopArea && offset > point) {
          // tell activity to show title
          showingTopArea = false
          (activity as? ProfileFragmentCallback)?.onHideTopArea()
        } else if (!showingTopArea && offset < point) {
          // hide title
          showingTopArea = true
          (activity as? ProfileFragmentCallback)?.onShowTopArea()
        }
      }
    })
  }

  override fun adapter(): BaseAdapter<Review>? {
    user?.let { user ->
      val isSelf = user.id == service.currentUser?.id ?: 0
      val adapter = ProfileAdapter(user, service, isSelf)

      adapter.actionClick.filterLoggedIn(baseActivity).subscribe {

        if (isSelf) {
          // profile setting
          startActivity(Intent(activity, ProfileEditActivity::class.java))
        } else {
          // chat
          applyIfVerified(baseActivity) {
            service.chatRooms.createChatRoom(user.id).subscribe({
              Timber.i("success create chat with : ${user.id}")
              tracker.trackChatRoomCreate()
              val intent = Intent(activity, ChatActivity::class.java)
              intent.putExtra(ChatActivity.EXTRA_ROOM_ID, it.id)
              startActivity(intent)
            }, {
              Timber.e(it, "fail create chat with : ${user.id}")
            })
          }
        }
      }

      adapter.reviewClick.subscribe {
        it.author?.let {
          val intent = Intent(activity, ProfileActivity::class.java)
          intent.putExtra(ProfileActivity.EXTRA_USER, it.toJson())
          startActivity(intent)
        }
      }

      adapter.bookClick.subscribe {
        it.book?.let {
          val intent = Intent(activity, BookActivity::class.java)
          intent.putExtra(BookActivity.EXTRA_BOOK, it.toJson())
          startActivity(intent)
        }
      }

      adapter.bookHeaderClick.subscribe {
        val intent = Intent(activity, SellingActivity::class.java)
        intent.putExtra(SellingActivity.EXTRA_USER_ID, user.id)
        startActivity(intent)
      }

      adapter.avatarClick
          .filter { user.profileIcon != null }
          .subscribe {
            val intent = Intent(activity, ImageViewerActivity::class.java)
            intent.putExtra(ImageViewerActivity.EXTRA_URL, user.profileIcon)
            startActivity(intent)
          }

      return adapter
    }
    return null
  }

  override fun fetchStream(page: Int): Single<List<Review>>? {
    user?.let { user ->
      val obs = service.api.getReviews(user.id, page).map { it.reviews.filter { it.author != null } }
      if (page == START_PAGE && user.id == service.currentUser?.id ?: 0) {
        return service.users.refreshUser()
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { (recyclerview.adapter as ProfileAdapter).user = user() ?: user }
            .observeOn(Schedulers.newThread())
            .flatMap { obs }
      }
      return obs
    }
    return null
  }

  fun user(): User? {
    if (arguments.containsKey(ARG_USER)) {
      return arguments.getString(ARG_USER)!!.toObject(User::class.java)!!
    } else {
      return service.currentUser
    }
  }
}
