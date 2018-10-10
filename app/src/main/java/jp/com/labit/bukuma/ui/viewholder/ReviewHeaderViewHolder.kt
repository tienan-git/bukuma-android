package jp.com.labit.bukuma.ui.viewholder

import android.view.ViewGroup
import android.widget.TextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.realm.User

/**
 * Created by zoonooz on 11/7/2016 AD.
 * Review header view holder
 */
class ReviewHeaderViewHolder(parent: ViewGroup) :
    BaseObjectViewHolder<User>(parent, R.layout.viewholder_review_header) {

  val happyTextView = itemView.findViewById(R.id.happy_textview) as TextView
  val fairTextView = itemView.findViewById(R.id.fair_textview) as TextView
  val sadTextView = itemView.findViewById(R.id.sad_textview) as TextView

  override fun setObject(obj: User) {
    happyTextView.text = "${obj.moodPositive}"
    fairTextView.text = "${obj.moodSoso}"
    sadTextView.text = "${obj.moodNegative}"
  }
}