package jp.com.labit.bukuma.ui.adapter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import io.realm.Realm
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.realm.Category
import jp.com.labit.bukuma.ui.viewholder.CategoryViewHolder
import rx.Observable
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 9/13/2016 AD.
 * Category adapter
 */
open class CategoryAdapter(var parentId: Int) : BaseAdapter<Category>() {

  var categoryClick: Observable<Pair<Category, Int?>> = PublishSubject.create()

  var showColor = false
  var showOption = false
  var showAll = false

  lateinit var realm: Realm

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
    super.onAttachedToRecyclerView(recyclerView)
    realm = Realm.getDefaultInstance()
    // find all level 1 category
    items = realm.where(Category::class.java)
        .equalTo("categoryId", parentId)
        .greaterThan("id", 0)
        .findAll()
  }

  override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
    realm.close()
    super.onDetachedFromRecyclerView(recyclerView)
  }

  override fun getItemCount(): Int {
    return super.getItemCount() + if (showAll) 1 else 0
  }

  override fun getRealItemPosition(position: Int): Int {
    return position - if (showAll) 1 else 0
  }

  override fun getItemViewType(position: Int): Int {
    return 0
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return CategoryViewHolder(parent)
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val context = holder.itemView.context
    if (holder is CategoryViewHolder) {
      if (showAll && position == 0) {
        val allCat = Category()
        allCat.id = parentId
        allCat.name = context.getString(R.string.search_category_all)
        holder.setObject(allCat, null)
        RxView.clicks(holder.itemView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
          (categoryClick as PublishSubject).onNext(Pair(allCat, null))
        }
      } else {
        val cat = getItem(position)
        val color = if (showColor) Color.parseColor(colors[position % colors.count()]) else null
        holder.setObject(cat, color)
        RxView.clicks(holder.itemView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
          (categoryClick as PublishSubject).onNext(Pair(cat, color))
        }
      }
    }
  }

  // color

  val colors = arrayOf(
      "#F45651", "#F46262", "#F06292", "#BA68C8", "#9575CD", "#7986CB",
      "#64B5F6", "#4FC3F7", "#4DD0E1", "#26C9C2", "#4DB6AC", "#81C784",
      "#AED581", "#DCE775", "#FFD54F", "#FFB74D", "#F37D27", "#A1887F",
      "#BDBDBD", "#90A4AE"
  )
}
