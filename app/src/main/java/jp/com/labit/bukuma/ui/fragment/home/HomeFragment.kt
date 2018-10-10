package jp.com.labit.bukuma.ui.fragment.home

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.jakewharton.rxbinding.view.RxView
import com.jakewharton.rxbinding.view.clicks
import jp.com.labit.bukuma.BukumaApplication
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.applyIfLoggedIn
import jp.com.labit.bukuma.extension.filterLoggedIn
import jp.com.labit.bukuma.fcm.NotificationCreator
import jp.com.labit.bukuma.ui.activity.ScannerActivity
import jp.com.labit.bukuma.ui.fragment.BaseFragment
import jp.com.labit.bukuma.ui.fragment.ProfileFragment
import kotlinx.android.synthetic.main.fragment_home.*
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 9/9/2016 AD.
 * Home fragment for browse book, chat, profile
 */
class HomeFragment : BaseFragment() {

  val browseFragment = BrowseFragment()
  val searchFragment = SearchFragment.newInstance()
  var isShowingFirstTab = true
  private var notiSubscription: Subscription? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return inflater.inflate(R.layout.fragment_home, container, false)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    chat_count_textview.visibility = View.GONE
    // start with list button
    onBottomMenuClick(list_button)
    // subscribe event
    subscribe(list_button)
    subscribe(search_cat_button)
    subscribe(chat_button)
    subscribe(profile_button)

    RxView.clicks(camera_button)
        .throttleFirst(1, TimeUnit.SECONDS)
        .filterLoggedIn(baseActivity)
        .subscribe {
          // show camera
          startActivity(Intent(activity, ScannerActivity::class.java))
        }

    updateBadge()

    notiSubscription = (activity.application as BukumaApplication)
        .notificationObservable
        .observeOn(AndroidSchedulers.mainThread())
        .filter { it.type == NotificationCreator.Type.MESSAGE }
        .subscribe { updateBadge() }
  }

  override fun onDestroyView() {
    notiSubscription?.unsubscribe()
    super.onDestroyView()
  }

  override fun onResume() {
    super.onResume()
    // check user login status and move to first tab if no user
    if (service.currentUser == null && (chat_button.isSelected || profile_button.isSelected)) {
      onBottomMenuClick(list_button)
    }
  }

  fun updateBadge() {
    if (service.currentUser == null) return
    service.chatRooms.getChatRooms(null)
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          Timber.i("check new message count success")
          if (chat_button != null && !chat_button.isSelected) {
            var chatCount = it
                .map { it.unreadCount }
                .fold(0) { i, j -> i + j }
            if (chatCount > 99) chatCount = 99
            chat_count_textview?.visibility = if (chatCount > 0) View.VISIBLE else View.INVISIBLE
            chat_count_textview?.text = "$chatCount"
          }
        }, {
          Timber.e(it, "check new message count error")
        })
  }

  /**
   * add button subscribe
   */
  fun subscribe(button: ImageButton) {
    button.clicks().throttleFirst(500, TimeUnit.MILLISECONDS).subscribe { onBottomMenuClick(button) }
  }

  /**
   * Set select tab button
   */
  fun onBottomMenuClick(button: ImageButton) {
    if (button.isSelected) return

    fun updateButtonState(resId: Int) {
      // clear button state
      list_button.apply {
        setImageResource(R.drawable.ic_tab_home)
        isSelected = false
      }
      search_cat_button.apply {
        setImageResource(R.drawable.ic_tab_search)
        isSelected = false
      }
      chat_button.apply {
        setImageResource(R.drawable.ic_tab_chat)
        isSelected = false
      }
      profile_button.apply {
        setImageResource(R.drawable.ic_tab_mypage)
        isSelected = false
      }
      button.isSelected = true
      button.setImageResource(resId)
    }

    isShowingFirstTab = false

    when (button) {
      list_button -> {
        updateButtonState(R.drawable.ic_tab_home_active)
        showFragment(browseFragment)
        isShowingFirstTab = true
      }
      search_cat_button -> {
        updateButtonState(R.drawable.ic_tab_search_active)
        showFragment(searchFragment)
      }
      chat_button -> {
        applyIfLoggedIn(baseActivity) {
          showFragment(ChatRoomFragment())
          updateButtonState(R.drawable.ic_tab_chat_active)
          chat_count_textview.visibility = View.INVISIBLE
        }
      }
      profile_button -> {
        applyIfLoggedIn(baseActivity) {
          showFragment(ProfileFragment.newInstance())
          updateButtonState(R.drawable.ic_tab_mypage_active)
        }
      }
    }
  }

  /**
   * Replace fragment to container
   */
  fun showFragment(fragment: Fragment) {
    childFragmentManager.beginTransaction().replace(R.id.home_container, fragment).commit()
  }

  /**
   * Show first tab. for main activity
   */
  fun showFirstTab() {
    onBottomMenuClick(list_button)
  }
}
