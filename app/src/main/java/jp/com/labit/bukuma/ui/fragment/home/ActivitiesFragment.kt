package jp.com.labit.bukuma.ui.fragment.home

import android.content.Intent
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.getAsObjectOrNull
import jp.com.labit.bukuma.extension.getAsPrimitiveOrNull
import jp.com.labit.bukuma.extension.toJson
import jp.com.labit.bukuma.model.Activity
import jp.com.labit.bukuma.ui.activity.BookActivity
import jp.com.labit.bukuma.ui.activity.ProfileActivity
import jp.com.labit.bukuma.ui.adapter.ActivitiesAdapter
import jp.com.labit.bukuma.ui.adapter.BaseAdapter
import jp.com.labit.bukuma.ui.fragment.BaseListFragment
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber

/**
 * Created by zoonooz on 10/6/2016 AD.
 * Activities fragment
 */
class ActivitiesFragment : BaseListFragment<Activity>() {

  override val emptyTitleText: String? get() = getString(R.string.activities_empty_title)
  override val emptyDescriptionText: String? get() = getString(R.string.activities_empty_description)
  override val emptyImageResourceId: Int get() = R.drawable.img_ph_02

  override fun adapter(): BaseAdapter<Activity>? {
    val adapter = ActivitiesAdapter()

    adapter.itemClick.subscribe {
      val bookId = if (it.key == Activity.KEY_MERCHANDISE_BOUGHT) {
        it.target
            ?.getAsObjectOrNull("book")
            ?.getAsPrimitiveOrNull("id")
            ?.asInt
      } else {
        it.target?.getAsPrimitiveOrNull("id")?.asInt
      }
      bookId?.let {
        service.api.getBook(it)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
              Timber.i("load book success")
              if (activity != null) {
                val intent = Intent(activity, BookActivity::class.java)
                intent.putExtra(BookActivity.EXTRA_BOOK, it.book!!.toJson())
                startActivity(intent)
              }
            }, {
              Timber.e(it, "load book error")
            })
      }
    }

    adapter.userClick.subscribe {
      it.user?.let {
        val intent = Intent(activity, ProfileActivity::class.java)
        intent.putExtra(ProfileActivity.EXTRA_USER, it.toJson())
        startActivity(intent)
      }
    }

    return adapter
  }

  override fun fetchStream(page: Int): Single<List<Activity>>? {
    val supportType = Activity.supportTypes()
    return service.api.getActivities(page)
        .doOnSuccess {
          if (page == START_PAGE) it.activities
              .firstOrNull()
              ?.let { preference.lastActivitiesId = it.id }
        }
        .map { it.activities.filter { supportType.contains(it.key) } }
  }
}
