package jp.com.labit.bukuma.ui.dialog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.api.response.BaseResponse
import jp.com.labit.bukuma.ui.activity.LoginActivity
import jp.com.labit.bukuma.ui.activity.RegisterActivity
import jp.com.labit.bukuma.util.RxAlertDialog
import jp.com.labit.bukuma.util.RxFacebook
import jp.com.labit.bukuma.util.infoDialog
import jp.com.labit.bukuma.util.loadingDialog
import kotlinx.android.synthetic.main.dialog_header.*
import kotlinx.android.synthetic.main.dialog_login.*
import retrofit2.adapter.rxjava.HttpException
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 9/15/2016 AD.
 * Login dialog
 */
class LoginDialog : BaseDialog(), FacebookCallback<LoginResult> {

  /**
   * Two mode, login and register
   */
  var isLoginMode = false

  lateinit var fbCallback: CallbackManager

  override fun onAttach(context: Context) {
    super.onAttach(context)
    FacebookSdk.sdkInitialize(context.applicationContext)
    fbCallback = CallbackManager.Factory.create()
    LoginManager.getInstance().registerCallback(fbCallback, this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return inflater.inflate(R.layout.dialog_login, container)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    header_imageview.setImageResource(R.drawable.img_bg_alldone)
    title_textview.text = getString(R.string.login_title, config.initialPoint)
    desc_textview.text = getString(R.string.login_description, config.initialPoint)

    // mode
    RxView.clicks(mode_button).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      isLoginMode = !isLoginMode
      if (isLoginMode) {
        mode_button.text = getText(R.string.login_dialog_login)
        email_button.text = getText(R.string.login_email)
      } else {
        mode_button.text = getText(R.string.login_dialog_register)
        email_button.text = getText(R.string.register_email)
      }
    }

    // register
    RxView.clicks(email_button).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      if (!isLoginMode) {
        startActivity(Intent(activity, RegisterActivity::class.java))
      } else {
        startActivity(Intent(activity, LoginActivity::class.java))
      }
      dismiss()
    }

    // facebook
    RxView.clicks(facebook_button).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      LoginManager.getInstance().logInWithReadPermissions(this, arrayListOf("email"))
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    fbCallback.onActivityResult(requestCode, resultCode, data)
  }

  // facebook

  override fun onCancel() {
    Timber.i("cancel login facebook")
  }

  override fun onError(error: FacebookException) {
    Timber.e(error, "error login facebook")
    RxAlertDialog.alert(
        activity, null,
        getString(R.string.login_dialog_error),
        getString(R.string.ok))
        .subscribe()
  }

  override fun onSuccess(result: LoginResult) {
    Timber.d("success login facebook token : ${result.accessToken}")

    RxFacebook.getProfile()
        .flatMap {
          Single.zip(
              Single.just(it),
              service.api.checkIfSnsUserExist("facebook", it.id).onErrorReturn {
                if (it is HttpException && it.code() == 404) {
                  val res = BaseResponse()
                  res.result = "false" // means not found
                  res
                } else {
                  throw IllegalStateException("server cannot check sns existing")
                }
              },
              { a, b -> Pair(a, b) })
        }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          if (activity == null) return@subscribe
          // redirect
          if (it.second.result == "success") {
            // found account -> login
            Timber.i("sns account exist -> login")
            val progress = loadingDialog(activity)
            service.users.loginWithFacebook(it.first.token)
                .doOnEach { progress.dismiss() }
                .subscribe({
                  Timber.i("login with facebook success")
                  infoDialog(activity, getString(R.string.login_dialog_success))
                  dismiss()
                }, {
                  Timber.e(it, "login with facebook error")
                  infoDialog(activity, getString(R.string.login_dialog_error))
                })
          } else {
            // not found -> register
            Timber.i("sns account not found -> register")
            val profile = it.first
            val intent = Intent(activity, RegisterActivity::class.java)
            intent.putExtra(RegisterActivity.EXTRA_PROVIDER, RegisterActivity.PROVIDER_FACEBOOK)
            intent.putExtra(RegisterActivity.EXTRA_EMAIL, profile.email)
            intent.putExtra(RegisterActivity.EXTRA_NICKNAME, profile.firstName)
            intent.putExtra(RegisterActivity.EXTRA_FACEBOOK_AVATAR, profile.picture)
            intent.putExtra(RegisterActivity.EXTRA_FACEBOOK_UID, profile.id)
            intent.putExtra(RegisterActivity.EXTRA_FACEBOOK_TOKEN, profile.token)
            startActivity(intent)
            dismiss()
          }
        }, {
          Timber.e(it, "get profile error")
        })
  }
}
