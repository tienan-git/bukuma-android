package jp.com.labit.bukuma.ui.viewholder

import android.support.v4.content.res.ResourcesCompat
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.Tag

/**
 * Created by tani on 6/22/2017 AD.
 * Header for list items
 */
class TagViewHolder(parent: ViewGroup) :
    BaseObjectViewHolder<Tag>(parent, R.layout.viewholder_tag) {

  val context = parent.context

  val nameTextView = itemView.findViewById(R.id.name) as TextView
  val countView = itemView.findViewById(R.id.count_view) as LinearLayout
  val countTextView = itemView.findViewById(R.id.count) as TextView
  val plusView = itemView.findViewById(R.id.plus_view) as ImageView

  override fun setObject(obj: Tag) {
    nameTextView.text = "＃${obj.name}（${obj.booksCount}件）"
    countTextView.text = voteCountText(obj.votesCount)

    if (obj.isVoted) {
      countView.background = ResourcesCompat.getDrawable(context.resources,
          R.drawable.background_voted_tag, null)
      countTextView.setTextColor(
          ResourcesCompat.getColor(context.resources, android.R.color.white, null))
      plusView.visibility = View.INVISIBLE
    } else {
      countView.background = ResourcesCompat.getDrawable(context.resources,
          R.drawable.background_warm_pink_circle, null)
      countTextView.setTextColor(
          ResourcesCompat.getColor(context.resources, R.color.warm_pink, null))
      plusView.visibility = View.VISIBLE
    }
  }

  private fun voteCountText(count: Int): String {
    if (count < 1_000) return count.toString()
    if (count < 10_000) return ((count / 100) / 10.0).toString() + "K"
    if (count < 1_000_000) return (count / 1_000).toString() + "K"
    if (count < 10_000_000) return ((count / 100_000) / 10.0).toString() + "M"
    return (count / 1_000_000).toString() + "M"
  }

}
