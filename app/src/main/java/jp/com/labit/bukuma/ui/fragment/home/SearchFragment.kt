package jp.com.labit.bukuma.ui.fragment.home

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.jakewharton.rxbinding.support.v7.widget.RxSearchView
import com.jakewharton.rxbinding.view.RxMenuItem
import io.realm.Realm
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.realm
import jp.com.labit.bukuma.model.realm.Category
import jp.com.labit.bukuma.model.realm.SearchHistory
import jp.com.labit.bukuma.ui.activity.CategoryActivity
import jp.com.labit.bukuma.ui.activity.MainActivity
import jp.com.labit.bukuma.ui.activity.SearchResultActivity
import jp.com.labit.bukuma.ui.adapter.BaseAdapter
import jp.com.labit.bukuma.ui.adapter.SearchAdapter
import jp.com.labit.bukuma.ui.fragment.BaseListFragment
import jp.com.labit.bukuma.util.RxAlertDialog
import kotlinx.android.synthetic.main.fragment_list.*
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 9/13/2016 AD.
 * Search fragment
 */
class SearchFragment : BaseListFragment<Category>() {

  var searchView: SearchView? = null
  var isHideCategory = false

  private var searchSubscription: Subscription? = null

  companion object {
    val ARG_HIDE_CATEGORY = "arg_hide_category"

    fun newInstance(hideCategory: Boolean = false): SearchFragment {
      val fragment = SearchFragment()
      val args = Bundle()
      args.putBoolean(ARG_HIDE_CATEGORY, hideCategory)
      fragment.arguments = args
      return fragment
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)

    isHideCategory = arguments.getBoolean(ARG_HIDE_CATEGORY)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    refresh_layout.isEnabled = false
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    super.onCreateOptionsMenu(menu, inflater)
    inflater.inflate(R.menu.menu_search, menu)
    val searchItem = menu.findItem(R.id.search)

    if (activity is MainActivity) {
      searchItem.actionView = null
      RxMenuItem.clicks(searchItem)
          .throttleFirst(1, TimeUnit.SECONDS)
          .subscribe { (activity as? MainActivity)?.showSearch() }
    } else {
      searchView = searchItem.actionView as SearchView

      if (isHideCategory) {
        MenuItemCompat.expandActionView(searchItem)
        searchView?.onActionViewExpanded()
      }

      // switch adapter mode when focus change
      searchView?.setOnSearchClickListener {
        val adapter = recyclerview.adapter as SearchAdapter
        adapter.mode = SearchAdapter.MODE_RECENT
        adapter.notifyDataSetChanged()
      }

      searchView?.setOnCloseListener {
        val adapter = recyclerview.adapter as SearchAdapter
        adapter.mode = SearchAdapter.MODE_ALL
        adapter.notifyDataSetChanged()
        false
      }

      RxSearchView.queryTextChangeEvents(searchView!!)
          .doOnNext {
            val adapter = recyclerview.adapter as SearchAdapter
            if (it.isSubmitted) {
              // save history & redirect to result
              val query = it.queryText()
              if (!TextUtils.isEmpty(query)) {
                val realm = Realm.getDefaultInstance()
                realm.executeTransaction {
                  val history = SearchHistory(query.toString(), Date().time)
                  it.insertOrUpdate(history)
                }
                realm.close()

                // redirect
                val intent = Intent(activity, SearchResultActivity::class.java)
                intent.putExtra(SearchResultActivity.EXTRA_KEYWORD, query.toString())
                intent.putExtra(SearchResultActivity.EXTRA_ORDER_ENABLE, true)
                startActivity(intent)

                // update items
                adapter.notifyDataSetChanged()
              }
            } else if (it.queryText().isBlank() && adapter.mode == SearchAdapter.MODE_RESULT) {
              adapter.mode = SearchAdapter.MODE_RECENT
              adapter.notifyDataSetChanged()
            }
          }
          .debounce(300, TimeUnit.MILLISECONDS)
          .filter { !it.isSubmitted && it.queryText().isNotBlank() }
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe {
            searchSubscription?.unsubscribe()
            searchSubscription = service.api.suggestWords(it.queryText().toString())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                  Timber.i("search title success")
                  val adapter = recyclerview.adapter as SearchAdapter
                  adapter.mode = SearchAdapter.MODE_RESULT
                  adapter.result = it.words
                  adapter.notifyDataSetChanged()
                }, {
                  Timber.e("search title error")
                })
          }
    }
  }

  override fun adapter(): BaseAdapter<Category> {
    val adapter = SearchAdapter()

    if (isHideCategory) adapter.mode = SearchAdapter.MODE_RECENT

    // show all book list if category item click
    adapter.categoryClick.subscribe {
      val cat = it.first
      val color = it.second
      // show sub category
      val intent = Intent(activity, CategoryActivity::class.java)
      intent.putExtra(CategoryActivity.EXTRA_PARENT_ID, cat.id)
      intent.putExtra(CategoryActivity.EXTRA_COLOR, color)
      intent.putExtra(CategoryActivity.EXTRA_FORCE_BOOK, true)
      startActivity(intent)
    }

    // show search result if history clicked
    adapter.historyClick.subscribe {
      val intent = Intent(activity, SearchResultActivity::class.java)
      intent.putExtra(SearchResultActivity.EXTRA_KEYWORD, it.text)
      startActivity(intent)
    }

    // confirm first before delete
    adapter.clearHistoryClick.subscribe {
      RxAlertDialog.alert2(
          activity,
          null,
          getString(R.string.search_history_clear_dialog_message),
          getString(R.string.search_history_clear_dialog_ok),
          getString(R.string.cancel))
          .subscribe {
            if (it) {
              realm { it.delete(SearchHistory::class.java) }
              adapter.notifyDataSetChanged()
            }
          }
    }

    // search result click
    adapter.resultClick.subscribe {
      val intent = Intent(activity, SearchResultActivity::class.java)
      intent.putExtra(SearchResultActivity.EXTRA_KEYWORD, it)
      startActivity(intent)
    }

    return adapter
  }
}
