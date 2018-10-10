package jp.com.labit.bukuma.ui.fragment

import android.content.Intent
import jp.com.labit.bukuma.api.BukumaApi
import jp.com.labit.bukuma.model.Announcement
import jp.com.labit.bukuma.ui.activity.WebActivity
import jp.com.labit.bukuma.ui.adapter.BaseAdapter
import jp.com.labit.bukuma.ui.adapter.BaseSimpleAdapter
import jp.com.labit.bukuma.ui.viewholder.NewsViewHolder
import rx.Single
import rx.schedulers.Schedulers

/**
 * Created by zoonooz on 9/15/2016 AD.
 * News fragment
 */
class NewsFragment : BaseListFragment<Announcement>() {

  override fun adapter(): BaseAdapter<Announcement> {
    val adapter = BaseSimpleAdapter(NewsViewHolder::class.java)

    adapter.itemClick.subscribe {
      it.url?.let {
        val intent = Intent(activity, WebActivity::class.java)
        intent.putExtra(WebActivity.EXTRA_URL, it)
        startActivity(intent)
      }
    }

    return adapter
  }

  override fun fetchStream(page: Int): Single<List<Announcement>> {
    return service.api.getAnnouncements(page).map { it.announcements }
  }

  override fun fetchDisplay(page: Int, list: List<Announcement>) {
    super.fetchDisplay(page, list)
    if (page == BukumaApi.START_PAGE) {
      list.firstOrNull()?.let { flagLatestRead(it.id) }
    }
  }

  /**
   * flag latest read at the server
   */
  fun flagLatestRead(latestId: Int) {
    service.api.markAnnouncementRead(latestId)
        .subscribeOn(Schedulers.newThread())
        .subscribe({}, {})
  }
}
