package jp.com.labit.bukuma.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import com.jakewharton.rxbinding.view.RxMenuItem
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.BukumaError
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.isValidEmail
import jp.com.labit.bukuma.model.realm.User
import jp.com.labit.bukuma.ui.custom.PicassoCircleTransform
import jp.com.labit.bukuma.util.infoDialog
import jp.com.labit.bukuma.util.loadingDialog
import kotlinx.android.synthetic.main.activity_register.*
import rx.Observable
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 9/20/2016 AD.
 * Register activity
 */
class RegisterActivity : BaseImageChooserActivity() {

  companion object {
    val EXTRA_PROVIDER = "extra_provider"
    val EXTRA_NICKNAME = "extra_nickname"
    val EXTRA_EMAIL = "extra_email"
    val EXTRA_FACEBOOK_TOKEN = "extra_fb_token"
    val EXTRA_FACEBOOK_UID = "extra_fb_uid"
    val EXTRA_FACEBOOK_AVATAR = "extra_fb_avatar"

    val PROVIDER_FACEBOOK = "facebook"
    val PROVIDER_EMAIL = "email"
  }

  var provider = PROVIDER_EMAIL
  var facebookToken: String? = null
  var facebookUid: String? = null
  var facebookAvatar: String? = null
  var avatarPath: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_register)

    supportActionBar?.title = getString(R.string.title_register)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    provider = intent.getStringExtra(EXTRA_PROVIDER) ?: PROVIDER_EMAIL

    // check the values
    if (provider == PROVIDER_FACEBOOK) {
      facebookToken = intent.getStringExtra(EXTRA_FACEBOOK_TOKEN)
      facebookUid = intent.getStringExtra(EXTRA_FACEBOOK_UID)
      facebookAvatar = intent.getStringExtra(EXTRA_FACEBOOK_AVATAR)

      if (facebookToken == null || facebookUid == null) {
        Timber.e("provider is facebook but token or uid is null")
        throw IllegalStateException("missing token or uid for facebook")
      }
    }

    nickname_form.requestFocus()

    // preset the values
    if (facebookAvatar != null) {
      picasso.load(facebookAvatar)
          .transform(PicassoCircleTransform())
          .fit()
          .into(avatar_imageview)
    } else {
      picasso.load(R.drawable.img_thumbnail_user)
          .transform(PicassoCircleTransform())
          .into(avatar_imageview)
    }
    nickname_form.text = intent.getStringExtra(EXTRA_NICKNAME) ?: ""
    email_form.text = intent.getStringExtra(EXTRA_EMAIL) ?: ""

    // actions
    Observable.merge(RxView.clicks(camera_button), RxView.clicks(avatar_imageview))
        .throttleFirst(1, TimeUnit.SECONDS)
        .subscribe { chooseImage() }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_done, menu)

    RxMenuItem.clicks(menu.findItem(R.id.done)).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      if (validateForm()) {
        val progress = loadingDialog(this)
        // register
        val gender = when (gender_form.spinnerView.selectedItemPosition) {
          1 -> User.GENDER_MALE
          2 -> User.GENDER_FEMALE
          3 -> User.GENDER_OTHER
          else -> User.GENDER_NONE
        }

        if (provider == PROVIDER_EMAIL) {
          service.users.registerUserByEmail(
              email_form.text,
              nickname_form.text,
              password_form.text,
              gender,
              invite_code_form.text,
              avatarPath
          )
              .doOnEach { progress.dismiss() }
              .subscribe({
                Timber.i("registered with email : ${email_form.text}")
                tracker.trackAccountRegister("email", true)
                startActivity(Intent(this, RegisterPhoneActivity::class.java))
                finish()
              }, {
                Timber.e(it, "register fail")
                tracker.trackAccountRegister("email", false)
                val error = BukumaError.errorType(it)
                if (error == BukumaError.Type.HttpError && error.customCode == -203) {
                  // email alr taken
                  infoDialog(this, getString(R.string.register_error_email_taken))
                } else {
                  infoDialog(this, getString(R.string.error_tryagain))
                }
              })
        } else if (provider == PROVIDER_FACEBOOK) {
          service.users.registerUserByFacebook(
              email_form.text,
              nickname_form.text,
              password_form.text,
              gender,
              facebookToken!!,
              facebookUid!!,
              facebookAvatar,
              invite_code_form.text,
              avatarPath)
              .doOnEach { progress.dismiss() }
              .subscribe({
                Timber.i("registered with facebook : ${email_form.text}")
                tracker.trackAccountRegister("facebook", true)
                startActivity(Intent(this, RegisterPhoneActivity::class.java))
                finish()
              }, {
                Timber.e(it, "register with facebook fail")
                tracker.trackAccountRegister("facebook", false)
                infoDialog(this, getString(R.string.error_tryagain))
              })
        }
      }
    }

    return super.onCreateOptionsMenu(menu)
  }

  // image chooser

  override fun onGotImage(path: String) {
    Timber.i("choosen image at : $path")
    picasso.load("file:$path").fit().centerCrop()
        .transform(PicassoCircleTransform())
        .into(avatar_imageview)
    avatarPath = path
  }

  // Utils

  fun validateForm(): Boolean {
    var isOk = true

    // nickname
    if (nickname_form.text.isBlank()) {
      isOk = false
      nickname_form.error = getString(R.string.register_error_nickname_empty)
    }

    // email
    if (email_form.text.isBlank()) {
      isOk = false
      email_form.error = getString(R.string.register_error_email_empty)
    } else if (!email_form.text.isValidEmail()) {
      isOk = false
      email_form.error = getString(R.string.register_error_email_invalid)
    }

    // password
    if (password_form.text.isBlank()) {
      isOk = false
      password_form.error = getString(R.string.register_error_password_empty)
    } else if (password_confirm_form.text.isBlank()) {
      isOk = false
      password_confirm_form.error = getString(R.string.register_error_password_confirm_empty)
    } else if (password_form.text != password_confirm_form.text) {
      isOk = false
      password_confirm_form.error = getString(R.string.register_error_password_notmatch)
    }

    // gender
    if (gender_form.text == getString(R.string.gender_none)) {
      isOk = false
      infoDialog(this, getString(R.string.register_error_gender))
    }

    return isOk
  }
}
