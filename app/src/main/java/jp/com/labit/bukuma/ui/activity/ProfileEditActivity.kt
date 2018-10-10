package jp.com.labit.bukuma.ui.activity

import android.app.ProgressDialog
import android.os.Bundle
import android.view.Menu
import com.jakewharton.rxbinding.view.RxMenuItem
import com.jakewharton.rxbinding.view.RxView
import com.jakewharton.rxbinding.widget.RxTextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.Response
import jp.com.labit.bukuma.extension.toResponse
import jp.com.labit.bukuma.model.realm.User
import jp.com.labit.bukuma.ui.custom.PicassoCircleTransform
import jp.com.labit.bukuma.util.RxAlertDialog
import jp.com.labit.bukuma.util.infoDialog
import kotlinx.android.synthetic.main.activity_profile_edit.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 9/28/2016 AD.
 * Profile edit activity
 */
class ProfileEditActivity : BaseImageChooserActivity() {

  lateinit var user: User
  var avatarPath: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_profile_edit)

    supportActionBar?.title = getString(R.string.title_profile_edit)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    nickname_form.requestFocus()

    user = service.currentUser!!
    nickname_form.text = user.nickname
    bio_edittext.setText(user.biography)
    if (user.profileIcon != null) {
      picasso.load(user.profileIcon).transform(PicassoCircleTransform()).fit().centerCrop().into(avatar_imageview)
    } else {
      picasso.load(R.drawable.img_thumbnail_user).transform(PicassoCircleTransform()).into(avatar_imageview)
    }

    val gender = user.gender ?: User.GENDER_NONE
    val genderIndex = when (gender) {
      User.GENDER_MALE -> 1
      User.GENDER_FEMALE -> 2
      User.GENDER_OTHER -> 3
      else -> 0
    }
    gender_form.spinnerView.setSelection(genderIndex)

    // actions
    Observable.merge(RxView.clicks(camera_button), RxView.clicks(avatar_imageview))
        .throttleFirst(1, TimeUnit.SECONDS)
        .subscribe { chooseImage() }

    // update count
    RxTextView.textChanges(bio_edittext).subscribe {
      count_textview.text = "${it.length} / 400"
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_done, menu)

    // action flow
    val progress = ProgressDialog(this)
    progress.setMessage(getString(R.string.loading))
    progress.setCancelable(false)
    RxMenuItem.clicks(menu.findItem(R.id.done))
        .throttleFirst(1, TimeUnit.SECONDS)
        .filter { validate() }
        .doOnEach { progress.show() }
        .observeOn(Schedulers.newThread())
        .flatMap {
          val gender = when (gender_form.spinnerView.selectedItemPosition) {
            1 -> User.GENDER_MALE
            2 -> User.GENDER_FEMALE
            else -> User.GENDER_OTHER
          }
          service.users.updateUser(
              user.email,
              nickname_form.text,
              gender,
              bio_edittext.text.toString(),
              avatarPath).toObservable().toResponse()
        }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext {
          progress.dismiss()
          if (it is Response.Error) {
            Timber.e(it.error, "update profile error")
            infoDialog(this, getString(R.string.error_tryagain))
          }
        }
        .filter { it is Response.Success }
        .flatMap {
          RxAlertDialog.alert(this, null,
              getString(R.string.profile_edit_success_message),
              getString(R.string.ok)).toObservable()
        }
        .subscribe {
          Timber.i("update profile success")
          finish()
        }

    return super.onCreateOptionsMenu(menu)
  }

  fun validate(): Boolean {
    var isOk = true

    // nickname
    if (nickname_form.text.isBlank()) {
      isOk = false
      nickname_form.error = getString(R.string.register_error_nickname_empty)
    }

    // bio
    if (bio_edittext.text.isBlank()) {
      isOk = false
      bio_edittext.error = getString(R.string.profile_edit_bio_error_empty)
    }

    // gender
    if (gender_form.text == getString(R.string.gender_none)) {
      isOk = false
      infoDialog(this, getString(R.string.register_error_gender))
    }
    return isOk
  }

  override fun onGotImage(path: String) {
    Timber.i("choosen image at : $path")
    picasso.load("file:$path").fit().centerCrop()
        .transform(PicassoCircleTransform())
        .into(avatar_imageview)
    avatarPath = path
  }
}
