package jp.com.labit.bukuma.ui.viewholder

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import jp.com.labit.bukuma.R

/**
 * Created by tani on 6/22/2017 AD.
 * Header for list items
 */
class HeaderWithCircleButtonViewHolder(parent: ViewGroup) :
    BaseObjectViewHolder<String>(parent, R.layout.viewholder_header_with_circle_button) {

  val context: Context = parent.context

  val nameTextView = itemView.findViewById(R.id.name) as TextView
  val circleButtonView = itemView.findViewById(R.id.circle_button_view) as LinearLayout
  val circleButtonTextView = itemView.findViewById(R.id.circle_button_text) as TextView

  var circleButtonTitle: CharSequence
    get() = circleButtonTextView.text
    set(value) { circleButtonTextView.text = value }

  override fun setObject(obj: String) {
    nameTextView.text = obj
  }

  fun setCircleButtonHighLighted(highLighted: Boolean) {

    circleButtonView.background = ContextCompat.getDrawable(context,
        if (highLighted) R.drawable.background_warm_pink_circle
        else R.drawable.background_grey_circle)
    circleButtonTextView.setTextColor(ContextCompat.getColor(context,
        if (highLighted) R.color.warm_pink
        else R.color.black54))
  }

}
