package jp.com.labit.bukuma.ui.viewholder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import jp.com.labit.bukuma.BukumaApplication
import javax.inject.Inject

/**
 * Created by zoonooz on 9/28/2016 AD.
 * Base view holder for injection
 */
abstract class BaseViewHolder(parent: ViewGroup, layoutId: Int) :
    RecyclerView.ViewHolder(getView(parent, layoutId)) {

  companion object {
    fun getView(parent: ViewGroup, layoutId: Int): View {
      val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
      return view
    }
  }

  @Inject lateinit var picasso: Picasso

  init {
    BukumaApplication.mainComponent.inject(this)
  }
}