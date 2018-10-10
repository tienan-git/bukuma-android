package jp.com.labit.bukuma.ui.viewholder

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.BlockUser
import jp.com.labit.bukuma.ui.custom.PicassoCircleTransform

/**
 * Created by zoonooz on 10/3/2016 AD.
 * Block user view holder
 */
class BlockViewHolder(parent: ViewGroup) : BaseObjectViewHolder<BlockUser>(parent, R.layout.viewholder_block_user) {

  val avatarImageView = itemView.findViewById(R.id.avatar_imageview) as ImageView
  val nameTextView = itemView.findViewById(R.id.name_textview) as TextView

  override fun setObject(obj: BlockUser) {
    obj.target?.let {
      nameTextView.text = it.nickname

      if (it.profileIcon != null) {
        picasso.load(it.profileIcon).transform(PicassoCircleTransform()).centerCrop().fit().into(avatarImageView)
      } else {
        picasso.load(R.drawable.img_thumbnail_user).transform(PicassoCircleTransform()).into(avatarImageView)
      }
    }
  }
}