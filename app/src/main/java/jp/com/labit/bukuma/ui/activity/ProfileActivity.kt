package jp.com.labit.bukuma.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import com.jakewharton.rxbinding.view.RxMenuItem
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.Response
import jp.com.labit.bukuma.extension.filterLoggedIn
import jp.com.labit.bukuma.extension.toObject
import jp.com.labit.bukuma.extension.toResponse
import jp.com.labit.bukuma.model.realm.User
import jp.com.labit.bukuma.ui.activity.drawer.ReportActivity
import jp.com.labit.bukuma.ui.fragment.ProfileFragment
import jp.com.labit.bukuma.util.RxAlertDialog
import kotlinx.android.synthetic.main.activity_profile.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 10/18/2016 AD.
 * User profile activity
 */
class ProfileActivity : BaseActivity(), ProfileFragment.ProfileFragmentCallback {

  var user: User? = null

  companion object {
    val EXTRA_USER = "extra_user"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_profile)
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = ""

    user = intent.getStringExtra(EXTRA_USER)?.toObject(User::class.java) ?: service.currentUser

    title_textview.visibility = View.INVISIBLE
    title_textview.text = user?.nickname ?: ""

    supportFragmentManager.beginTransaction()
        .replace(R.id.container, ProfileFragment.newInstance(user))
        .commit()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val currentUserId = service.currentUser?.id ?: 0
    if (user?.id ?: 0 != currentUserId) {
      menuInflater.inflate(R.menu.menu_profile, menu)

      // block
      RxMenuItem.clicks(menu.findItem(R.id.block))
          .throttleFirst(1, TimeUnit.SECONDS)
          .filterLoggedIn(this)
          .filter { user?.id ?: 0 != 0 }
          .flatMap {
            RxAlertDialog.alert2(this,
                null,
                getString(R.string.profile_block_confirm_message),
                getString(R.string.profile_block_confirm_ok),
                getString(R.string.cancel))
          }
          .filter { it }
          .observeOn(Schedulers.newThread())
          .flatMap { service.users.block(user!!.id).toObservable().toResponse() }
          .observeOn(AndroidSchedulers.mainThread())
          .filter { it is Response.Success }
          .flatMap {
            RxAlertDialog.alert(this,
                null,
                getString(R.string.profile_block_success_message),
                getString(R.string.ok)).toObservable()
          }
          .subscribe { finish() }

      // report
      RxMenuItem.clicks(menu.findItem(R.id.report))
          .throttleFirst(1, TimeUnit.SECONDS)
          .filterLoggedIn(this)
          .filter { user?.id ?: 0 != 0 }
          .flatMap {
            RxAlertDialog.alertN(this,
                getString(R.string.profile_report_type_message),
                resources.getStringArray(R.array.report_type_array),
                getString(R.string.cancel)).toObservable()
          }
          .filter { it != -1 }
          .map { resources.getStringArray(R.array.report_type_array)[it] }
          .observeOn(Schedulers.newThread())
          .flatMap { service.api.createReport(user!!.id, "User", it).toObservable().toResponse() }
          .observeOn(AndroidSchedulers.mainThread())
          .filter { it is Response.Success }
          .flatMap {
            RxAlertDialog.alert2(this,
                getString(R.string.profile_report_success_title),
                getString(R.string.profile_report_success_message),
                getString(R.string.profile_report_success_report),
                getString(R.string.cancel))
          }
          .filter { it }
          .subscribe {
            val intent = Intent(this, ReportActivity::class.java)
            intent.putExtra(ReportActivity.EXTRA_OPPONENT_ID, user!!.id)
            intent.putExtra(ReportActivity.EXTRA_OPPONENT_NAME, user!!.nickname)
            startActivity(intent)
          }
    }
    return true
  }

  // profile fragment callback

  override fun onHideTopArea() {
    Timber.e("hide top")
    title_textview.visibility = View.VISIBLE
  }

  override fun onShowTopArea() {
    Timber.e("show top")
    title_textview.visibility = View.INVISIBLE
  }
}
