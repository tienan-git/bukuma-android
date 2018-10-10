package jp.com.labit.bukuma.ui.viewholder

import android.view.ViewGroup
import android.widget.ImageView
import jp.com.labit.bukuma.R

/**
 * Created by zoonooz on 2017/01/23.
 * Banner view holder
 */
class BannerViewHolder(parent: ViewGroup) : BaseObjectViewHolder<String>(parent, R.layout.viewholder_banner) {

  val imageView = itemView.findViewById(R.id.banner_imageview) as ImageView

  override fun setObject(obj: String) {
    picasso.load(obj).into(imageView)
  }
}