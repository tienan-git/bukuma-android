package jp.com.labit.bukuma.ui.activity

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.Book
import jp.com.labit.bukuma.ui.fragment.BaseBookListFragment
import jp.com.labit.bukuma.util.manipulateColor
import kotlinx.android.synthetic.main.fragment_list.*
import rx.Single

/**
 * Created by tani on 2017/06/02.
 */
class TimelineActivity : BaseActivity() {
  companion object {
    val EXTRA_URL = "extra_url"
    val EXTRA_COLOR = "extra_color"
    val EXTRA_TITLE = "extra_title"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_container)

    // toolbar color
    val color = intent.getIntExtra(TimelineActivity.EXTRA_COLOR, 0)
    if (color != 0) {
      supportActionBar?.setBackgroundDrawable(ColorDrawable(color))
      if (Build.VERSION.SDK_INT >= 21) {
        window.statusBarColor = manipulateColor(color, 0.9f)
      }
    }

    val title = intent.getStringExtra(TimelineActivity.EXTRA_TITLE)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = title

    val url = intent.getStringExtra(TimelineActivity.EXTRA_URL)
    val fragment = TimelineFragment.newInstance(url)
    supportFragmentManager.beginTransaction()
        .replace(R.id.container, fragment)
        .commit()
  }

  class TimelineFragment : BaseBookListFragment() {
    override val emptyTitleText: String? get() = getString(R.string.search_empty_title)
    override val emptyDescriptionText: String? get() = getString(R.string.search_empty_description)
    override val emptyImageResourceId: Int get() = R.drawable.img_ph_01

    var url = ""
    var title = ""

    companion object {
      val ARG_URL = "arg_url"

      fun newInstance(url: String): TimelineFragment {
        val fragment = TimelineFragment()
        val args = Bundle()
        args.putString(ARG_URL, url)
        fragment.arguments = args
        return fragment
      }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
      url = arguments.getString(ARG_URL)
      super.onCreate(savedInstanceState)
    }

    override fun fetchStream(page: Int): Single<List<Book>>? {
      return service.api.getBooks(url, page).map {
        title = it.title
        it.books
      }
    }

    override fun fetchDisplay(page: Int, list: List<Book>) {
      super.fetchDisplay(page, list)
      (activity as? AppCompatActivity)?.supportActionBar?.let {
        if (it.title.isNullOrEmpty()) {
          it.title = title
        }
      }
    }

    override fun hideEmptyView() {
      super.hideEmptyView()
      recyclerview.setBackgroundResource(android.R.color.transparent)
    }
  }
}