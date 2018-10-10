package jp.com.labit.bukuma.ui.viewholder

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.realm.User
import jp.com.labit.bukuma.ui.custom.PicassoCircleTransform

/**
 * Created by zoonooz on 9/28/2016 AD.
 * User header info for profile page
 */
class UserInfoViewHolder(parent: ViewGroup, val isSelf: Boolean) : BaseObjectViewHolder<User>(parent, R.layout.viewholder_user_info) {

  val avatarImageView = itemView.findViewById(R.id.avatar_imageview) as ImageView
  var nameTextView = itemView.findViewById(R.id.name_textview) as TextView
  val bioTextView = itemView.findViewById(R.id.bio_textview) as TextView
  val actionButton = itemView.findViewById(R.id.action_button) as Button
  val officialImageView = itemView.findViewById(R.id.official_imageView) as ImageView
  val officialTextView = itemView.findViewById(R.id.official_textview) as TextView

  override fun setObject(obj: User) {
    nameTextView.text = obj.nickname
    bioTextView.text = obj.biography

    if (obj.profileIcon != null) {
      picasso.load(obj.profileIcon).transform(PicassoCircleTransform()).fit().centerCrop().into(avatarImageView)
    } else {
      picasso.load(R.drawable.img_thumbnail_user).transform(PicassoCircleTransform()).into(avatarImageView)
    }

    val context = itemView.context
    if (isSelf) {
      // current user
      actionButton.text = context.getString(R.string.profile_action_setting)
    } else {
      // other user
      actionButton.text = context.getString(R.string.profile_action_chat)
    }

    if (obj.isOfficial) {
      officialTextView.text = context.getString(R.string.profile_official_account)
    } else {
      officialImageView.visibility = View.INVISIBLE
      officialTextView.visibility = View.INVISIBLE
    }
  }
}