package jp.com.labit.bukuma.manager

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import com.sonymobile.home.resourceprovider.BadgeProviderContract
import jp.com.labit.bukuma.BukumaApplication
import jp.com.labit.bukuma.custom.SafeAsyncQueryHandler
import jp.com.labit.bukuma.ui.activity.MainActivity
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber

/**
 * Created by tani on 2017/06/20.
 */

class IconBadgeManager(application: Application) {

  val packageName: String
  val activityName = MainActivity::class.java.name
  val queryHandler: SafeAsyncQueryHandler

  init {
    packageName = application.packageName
    queryHandler = object: SafeAsyncQueryHandler(application.contentResolver) {
      override fun onInsertComplete(token: Int, cookie: Any?, uri: Uri?) {
        Timber.d("Success")
      }

      override fun onError(token: Int, cookie: Any?, error: RuntimeException) {
        Timber.d("Failure")
      }
    }
  }

  fun setup(bukuma: BukumaApplication) {
    bukuma.notificationObservable
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          it.badgeCount?.let { showBadge(it) }
        }
  }

  fun showBadge(count: Int) {
    Timber.d("$count")

    val values = ContentValues()
    values.put(BadgeProviderContract.Columns.BADGE_COUNT, count)
    values.put(BadgeProviderContract.Columns.PACKAGE_NAME, packageName)
    values.put(BadgeProviderContract.Columns.ACTIVITY_NAME, activityName)

    queryHandler.startInsert(0, null, BadgeProviderContract.CONTENT_URI, values)
  }

  fun hideBadge() {
    showBadge(0)
  }
}