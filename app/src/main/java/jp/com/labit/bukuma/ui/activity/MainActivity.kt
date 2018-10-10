package jp.com.labit.bukuma.ui.activity

import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import com.jakewharton.rxbinding.view.RxView
import com.jakewharton.rxbinding.widget.RxTextView
import io.realm.Realm
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.realm
import jp.com.labit.bukuma.model.realm.SearchHistory
import jp.com.labit.bukuma.ui.activity.drawer.*
import jp.com.labit.bukuma.ui.activity.setting.SettingActivity
import jp.com.labit.bukuma.ui.adapter.SearchAdapter
import jp.com.labit.bukuma.ui.custom.SeparatorItemDecoration
import jp.com.labit.bukuma.ui.dialog.TagIntroductionDialog
import jp.com.labit.bukuma.ui.fragment.home.HomeFragment
import jp.com.labit.bukuma.util.RxAlertDialog
import jp.com.labit.bukuma.util.hideKeyboard
import jp.com.labit.bukuma.util.showKeyboard
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.drawer_navigation.*
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 9/8/2016 AD.
 * Main screen
 */
class MainActivity : BaseActivity() {
  lateinit var drawerToggle: ActionBarDrawerToggle

  private var searchSubscription: Subscription? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // set custom toolbar
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setDisplayShowTitleEnabled(false)

    // navigation logo
    supportActionBar?.setIcon(R.drawable.img_nav_logo)

    setupSearch()
    setupNavigationMenu()
    supportFragmentManager.beginTransaction()
        .replace(R.id.container, HomeFragment())
        .commit()

    // update news count
    updateUnreadNewsCount()

    if (!preference.displayedTagIntroduction) {
      preference.displayedTagIntroduction = true
      TagIntroductionDialog().show(supportFragmentManager, "tag_introduction")
    }
  }

  override fun onResume() {
    super.onResume()
    drawer_layout.closeDrawer(Gravity.START, false)
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    hideKeyboard(this)
    if (drawerToggle.onOptionsItemSelected(item)) {
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onBackPressed() {
    if (search_layout.visibility == View.VISIBLE) {
      hideSearch()
      return
    }
    val fragment = supportFragmentManager.findFragmentById(R.id.container) as? HomeFragment
    if (fragment != null && !fragment.isShowingFirstTab) {
      fragment.showFirstTab()
    } else super.onBackPressed()
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    drawerToggle.syncState()
  }

  /**
   * Setup left navigation drawer
   */
  fun setupNavigationMenu() {
    drawerToggle = ActionBarDrawerToggle(this, drawer_layout, 0, 0)
    drawer_layout.addDrawerListener(drawerToggle)

    val items = resources.getStringArray(R.array.navigation_items)
    val icons = resources.obtainTypedArray(R.array.navigation_icons)
    for (i in items.indices) {
      val itemView = layoutInflater.inflate(R.layout.drawer_navigation_item, item_layout, false)
      itemView.id = i + 1
      (itemView.findViewById(R.id.imageview) as ImageView).setImageResource(icons.getResourceId(i, 0))
      (itemView.findViewById(R.id.textview) as TextView).text = items[i]
      (itemView.findViewById(R.id.count)).visibility = View.GONE
      // action
      RxView.clicks(itemView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        onDrawerItemClick(i)
      }
      item_layout.addView(itemView)
    }
    icons.recycle()
  }

  fun onDrawerItemClick(position: Int) {
    when (position) {
      0 -> showHome()
      1 -> startActivity(Intent(this, NewsActivity::class.java))
      2 -> startActivity(Intent(this, LikeActivity::class.java))
      3 -> startActivity(Intent(this, SellingActivity::class.java))
      4 -> startActivity(Intent(this, BoughtActivity::class.java))
      5 -> startActivity(Intent(this, SettingActivity::class.java))
      6 -> showTutorial()
      7 -> startActivity(Intent(this, FrequentlyQuestionsActivity::class.java))
      8 -> startActivity(Intent(this, InviteActivity::class.java))
    }
  }

  fun showHome() {
    val fragment = supportFragmentManager.findFragmentById(R.id.container) as? HomeFragment
    if (!(fragment?.isShowingFirstTab ?: false)) {
      supportFragmentManager.beginTransaction()
          .replace(R.id.container, HomeFragment())
          .commitNow()
    }
    drawer_layout.closeDrawer(Gravity.START)
  }

  fun showTutorial() {
    val intent = Intent(this, WebActivity::class.java)
    intent.putExtra(WebActivity.EXTRA_TITLE, getString(R.string.navigation_howto))
    intent.putExtra(WebActivity.EXTRA_URL, "http://static.bukuma.io/bkm_app/how-to-use.html")
    startActivity(intent)
  }

  fun updateUnreadNewsCount() {
    if (service.currentUser != null) {
      service.api.getAnnouncementUnreadCount()
          .subscribeOn(Schedulers.newThread())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe({
            val countView = findViewById(2)?.findViewById(R.id.count) as? TextView
            if (it.unreadCount > 0) {
              countView?.text = "${it.unreadCount}"
              countView?.visibility = View.VISIBLE
            } else {
              countView?.visibility = View.GONE
            }
          }, {})
    }
  }

  // search

  private fun setupSearch() {
    // apply inset
    if (Build.VERSION.SDK_INT >= 21) {
      toolbar.setOnApplyWindowInsetsListener { view, windowInsets ->
        view.onApplyWindowInsets(windowInsets)
        search_layout.onApplyWindowInsets(windowInsets)
      }
    }

    // update view when keyboard is showing / hiding
    search_layout.viewTreeObserver.addOnGlobalLayoutListener {
      val r = Rect()
      search_layout.getWindowVisibleDisplayFrame(r)
      val kb = search_layout.height - r.bottom
      search_layout.setPadding(
          search_layout.paddingLeft,
          search_layout.paddingTop,
          search_layout.paddingRight,
          kb)
    }

    val searchAdapter = SearchAdapter()
    searchAdapter.mode = SearchAdapter.MODE_RECENT
    search_recyclerview.adapter = searchAdapter
    search_recyclerview.layoutManager = LinearLayoutManager(this)
    search_recyclerview.addItemDecoration(SeparatorItemDecoration())

    // search result click
    searchAdapter.resultClick.subscribe {
      val intent = Intent(this, SearchResultActivity::class.java)
      intent.putExtra(SearchResultActivity.EXTRA_KEYWORD, it)
      startActivity(intent)
    }

    // show search result if history clicked
    searchAdapter.historyClick.subscribe {
      val intent = Intent(this, SearchResultActivity::class.java)
      intent.putExtra(SearchResultActivity.EXTRA_KEYWORD, it.text)
      startActivity(intent)
    }

    // confirm first before delete
    searchAdapter.clearHistoryClick.subscribe {
      RxAlertDialog.alert2(
          this,
          null,
          getString(R.string.search_history_clear_dialog_message),
          getString(R.string.search_history_clear_dialog_ok),
          getString(R.string.cancel))
          .subscribe {
            if (it) {
              realm { it.delete(SearchHistory::class.java) }
              searchAdapter.notifyDataSetChanged()
            }
          }
    }

    Observable.merge(
        RxView.clicks(search_layout),
        RxView.clicks(search_close_button))
        .subscribe { hideSearch() }

    RxTextView.textChanges(searchview)
        .doOnNext { if (searchview.text.isEmpty()) resetSearch() }
        .debounce(300, TimeUnit.MILLISECONDS)
        .filter { it.isNotBlank() }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          searchSubscription?.unsubscribe()
          searchSubscription = service.api.suggestWords(it.toString())
              .subscribeOn(Schedulers.newThread())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe({
                Timber.i("search title success")
                val adapter = search_recyclerview.adapter as SearchAdapter
                adapter.mode = SearchAdapter.MODE_RESULT
                adapter.result = it.words
                adapter.notifyDataSetChanged()
              }, {
                Timber.e("search title error")
              })
        }

    RxTextView.editorActions(searchview)
        .filter { searchview.text.isNotEmpty() }
        .filter { it == EditorInfo.IME_ACTION_SEARCH }
        .subscribe {
          val query = searchview.text.toString()
          searchview.setText("")

          // save history & redirect to result
          if (!TextUtils.isEmpty(query)) {
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
              val history = SearchHistory(query, Date().time)
              it.insertOrUpdate(history)
            }
            realm.close()
            val intent = Intent(this, SearchResultActivity::class.java)
            intent.putExtra(SearchResultActivity.EXTRA_KEYWORD, query)
            startActivity(intent)
          }
        }

    //search_card.visibility = View.INVISIBLE
    search_recyclerview.visibility = View.GONE
  }

  fun showSearch() {
    search_layout.alpha = 0f
    search_layout.visibility = View.VISIBLE
    search_layout.animate().alpha(1f).withEndAction {
      search_recyclerview.visibility = View.VISIBLE
    }

    searchview.requestFocus()
    showKeyboard(this, searchview)
  }

  fun hideSearch() {
    search_layout.animate().alpha(0f).withEndAction {
      search_layout.visibility = View.GONE
      search_recyclerview.visibility = View.GONE
      searchview.setText("")
      hideKeyboard(this)
    }
  }

  private fun resetSearch() {
    searchSubscription?.unsubscribe()
    val adapter = search_recyclerview.adapter as SearchAdapter
    adapter.mode = SearchAdapter.MODE_RECENT
    adapter.notifyDataSetChanged()
  }
}
