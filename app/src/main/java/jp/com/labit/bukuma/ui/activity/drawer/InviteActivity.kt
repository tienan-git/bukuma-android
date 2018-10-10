package jp.com.labit.bukuma.ui.activity.drawer

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.filterLoggedIn
import jp.com.labit.bukuma.ui.activity.BaseActivity
import jp.com.labit.bukuma.util.openAppInStore
import jp.com.labit.bukuma.util.shareTextAndLink
import kotlinx.android.synthetic.main.activity_invite.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 9/16/2016 AD.
 * Invitation point activity
 */
class InviteActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_invite)

    setSupportActionBar(toolbar)
    supportActionBar?.title = ""
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    // description color
    val firstString = getString(R.string.invite_description1)
    val invitationPoint = config.invitationPoint
    val coloredString = getString(R.string.invite_description2, invitationPoint)
    val spanned = SpannableString(
        firstString +
            coloredString +
            getString(R.string.invite_description3))
    spanned.setSpan(
        ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimary)),
        firstString.length, firstString.length + coloredString.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    description_textview.text = spanned

    // update people count if logged in
    people_textview.text = getString(R.string.invite_code_people, 0)
    if (service.currentUser != null) {
      service.api.getInvitedCount()
          .subscribeOn(Schedulers.newThread())
          .observeOn(AndroidSchedulers.mainThread())
          .takeUntil(RxView.detaches(people_textview))
          .subscribe({
            Timber.i("get invited count success")
            people_textview.text = getString(R.string.invite_code_people, it.total)
          }, {
            Timber.e(it, "get invited count error")
          })
    }

    // share
    RxView.clicks(share_button).throttleFirst(1, TimeUnit.SECONDS)
        .filterLoggedIn(this)
        .subscribe {
          // share text with link
          val invitationPoint = config.invitationPoint
          val text = getString(
              R.string.invite_share_text,
              service.currentUser?.inviteCode ?: "",
              invitationPoint)
          val link = "http://hyperurl.co/01o4af"
          shareTextAndLink(this, text, link)
        }

    // review store
    RxView.clicks(review_button).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      openAppInStore(this)
    }
  }

  override fun onResume() {
    super.onResume()

    // update display data
    val user = service.currentUser
    if (user != null) {
      code_textview.text = user.inviteCode
    } else {
      code_textview.text = getString(R.string.invite_code_empty)
    }
  }
}
