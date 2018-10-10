package jp.com.labit.bukuma.ui.viewholder

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.realm.Category

/**
 * Created by zoonooz on 9/13/2016 AD.
 * Category view holder for search
 */
class CategoryViewHolder(parent: ViewGroup) : BaseObjectViewHolder<Category>(parent, R.layout.viewholder_category) {

  val circle: View = itemView.findViewById(R.id.circle)
  val nameTextView = itemView.findViewById(R.id.name) as TextView

  fun setObject(category: Category, color: Int?) {
    setObject(category)
    if (color != null) {
      circle.visibility = View.VISIBLE
      circle.setBackgroundColor(color)
    } else {
      circle.visibility = View.GONE
    }
  }

  override fun setObject(obj: Category) {
    nameTextView.text = obj.name
  }
}