package jp.com.labit.bukuma.ui.viewholder

import android.view.ViewGroup
import jp.com.labit.bukuma.api.BukumaService
import jp.com.labit.bukuma.model.Merchandise
import jp.com.labit.bukuma.ui.custom.HorizontalSnapHelper
import rx.Single

/**
 * Created by zoonooz on 11/7/2016 AD.
 * Profile book view holder
 */
class ProfileBookViewHolder(parent: ViewGroup, val userId: Int, val service: BukumaService) :
    BaseListViewHolder<Merchandise, ProfileBookItemViewHolder>(
        parent,
        ProfileBookItemViewHolder::class.java) {

  init {
    HorizontalSnapHelper().attachToRecyclerView(recyclerView)
  }

  override fun fetchStream(page: Int): Single<List<Merchandise>>? {
    return service.api.getMerchandisesFromUser(userId, page, 1).map { it.merchandises }
  }
}
