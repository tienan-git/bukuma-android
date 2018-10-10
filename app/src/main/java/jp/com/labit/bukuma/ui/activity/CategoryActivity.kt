package jp.com.labit.bukuma.ui.activity

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import io.realm.Realm
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.realm.Category
import jp.com.labit.bukuma.ui.adapter.BaseAdapter
import jp.com.labit.bukuma.ui.adapter.CategoryAdapter
import jp.com.labit.bukuma.ui.fragment.BaseListFragment
import jp.com.labit.bukuma.ui.fragment.BookCategoryFragment
import jp.com.labit.bukuma.util.manipulateColor
import kotlinx.android.synthetic.main.fragment_list.*

/**
 * Created by zoonooz on 9/13/2016 AD.
 * Activity that show list of category
 */
class CategoryActivity : BaseActivity() {

  companion object {
    val EXTRA_COLOR = "extra_color"
    val EXTRA_PARENT_ID = "extra_parent_id"
    val EXTRA_FORCE_BOOK = "extra_force_book"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_container)

    val parentId = intent.getIntExtra(EXTRA_PARENT_ID, 0)
    if (parentId == 0) finish()

    val realm = Realm.getDefaultInstance()
    val parent = realm.where(Category::class.java)
        .equalTo("id", parentId)
        .findFirst()

    // toolbar color
    val color = intent.getIntExtra(EXTRA_COLOR, 0)
    if (color != 0) {
      supportActionBar?.setBackgroundDrawable(ColorDrawable(color))
      if (Build.VERSION.SDK_INT >= 21) {
        window.statusBarColor = manipulateColor(color, 0.9f)
      }
    }

    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = parent.name

    // category or book mode
    val forceBook = intent.getBooleanExtra(EXTRA_FORCE_BOOK, false)
    val fragment = if (parent.categoriesCount == 0 || forceBook) {
      BookCategoryFragment.newInstance(parentId, true)
    } else {
      CategoryFragment.newInstance(parent.id, color)
    }
    supportFragmentManager.beginTransaction()
        .replace(R.id.container, fragment)
        .commit()

    realm.close()
  }

  class CategoryFragment : BaseListFragment<Category>() {
    companion object {
      val ARG_PARENT_ID = "arg_parent_id"
      val ARG_COLOR = "arg_color"

      fun newInstance(parentId: Int, color: Int? = null): CategoryFragment {
        val fragment = CategoryFragment()
        val args = Bundle()
        args.putInt(ARG_PARENT_ID, parentId)
        color?.let { args.putInt(ARG_COLOR, color) }
        fragment.arguments = args
        return fragment
      }
    }

    var parentId = 0
    var color = 0

    override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      parentId = arguments.getInt(ARG_PARENT_ID)
      color = arguments.getInt(ARG_COLOR)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
      super.onViewCreated(view, savedInstanceState)
      refresh_layout.isEnabled = false
    }

    override fun adapter(): BaseAdapter<Category> {
      val adapter = CategoryAdapter(parentId)
      adapter.showOption = true
      adapter.showAll = true

      adapter.categoryClick.subscribe {
        val cat = it.first
        val intent = Intent(activity, CategoryActivity::class.java)
        intent.putExtra(CategoryActivity.EXTRA_PARENT_ID, cat.id)
        intent.putExtra(CategoryActivity.EXTRA_COLOR, color)
        if (cat.name == getString(R.string.search_category_all)) {
          intent.putExtra(CategoryActivity.EXTRA_FORCE_BOOK, true)
        }
        startActivity(intent)
      }

      return adapter
    }
  }
}
