package jp.com.labit.bukuma.ui.viewholder

import android.support.v4.content.ContextCompat
import android.text.format.DateUtils
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.Review
import jp.com.labit.bukuma.ui.custom.PicassoCircleTransform

/**
 * Created by zoonooz on 9/28/2016 AD.
 * Review ViewHolder
 */
class ReviewViewHolder(parent: ViewGroup) : BaseObjectViewHolder<Review>(parent, R.layout.viewholder_review) {

  val avatarImageView = itemView.findViewById(R.id.avatar_imageview) as ImageView
  val nameTextView = itemView.findViewById(R.id.name_textview) as TextView
  val moodImageView = itemView.findViewById(R.id.mood_imageview) as ImageView
  val moodTextView = itemView.findViewById(R.id.mood_textview) as TextView
  val commentTextView = itemView.findViewById(R.id.comment_textview) as TextView
  val timeTextView = itemView.findViewById(R.id.time_textview) as TextView

  override fun setObject(obj: Review) {
    val context = itemView.context
    commentTextView.text = obj.comment

    // time
    timeTextView.text = DateUtils.getRelativeTimeSpanString((obj.updatedAt * 1000),
        System.currentTimeMillis(),
        0L,
        DateUtils.FORMAT_ABBREV_ALL)

    when (obj.mood) {
      Review.USER_MOOD_SOSO -> {
        moodImageView.setImageResource(R.drawable.ic_face_02)
        moodTextView.text = context.getString(R.string.profile_review_fair)
        moodTextView.setTextColor(ContextCompat.getColor(context, R.color.mood_fair))
      }
      Review.USER_MOOD_NEGATIVE -> {
        moodImageView.setImageResource(R.drawable.ic_face_03)
        moodTextView.text = context.getString(R.string.profile_review_bad)
        moodTextView.setTextColor(ContextCompat.getColor(context, R.color.mood_sad))
      }
      else -> {
        moodImageView.setImageResource(R.drawable.ic_face_01)
        moodTextView.text = context.getString(R.string.profile_review_good)
        moodTextView.setTextColor(ContextCompat.getColor(context, R.color.mood_happy))
      }
    }

    obj.author?.let {
      val imageUrl = it.profileIcon
      if (imageUrl != null) {
        picasso.load(imageUrl).transform(PicassoCircleTransform()).into(avatarImageView)
      } else {
        picasso.load(R.drawable.img_thumbnail_user).transform(PicassoCircleTransform()).into(avatarImageView)
      }

      nameTextView.text = it.nickname
    }
  }
}
