package jp.com.labit.bukuma.extension

import android.content.Intent
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.ui.activity.BaseActivity
import jp.com.labit.bukuma.ui.activity.RegisterPhoneActivity
import jp.com.labit.bukuma.ui.dialog.LoginDialog
import jp.com.labit.bukuma.util.RxAlertDialog

/**
 * Created by zoonooz on 10/18/2016 AD.
 * User related logic
 */

/**
 * Apply function if user logged in or show register popup
 *
 * @param context context to check user
 * @param f block that will run if user exist
 */
fun applyIfLoggedIn(context: BaseActivity, f: () -> Unit) {
  if (context.service.currentUser == null) {
    val dialog = LoginDialog()
    dialog.show(context.supportFragmentManager, "login")
  } else {
    f()
  }
}

/**
 * Apply function if user verified phone
 *
 * @param context context to check user
 * @param f block that will run if user verified
 */
fun applyIfVerified(context: BaseActivity, f: () -> Unit) {
  applyIfLoggedIn(context) {
    if (!context.service.currentUser!!.verified) {
      RxAlertDialog.alert2(context, null,
          context.getString(R.string.dialog_phone_not_verify_message),
          context.getString(R.string.dialog_phone_not_verify_ok),
          context.getString(R.string.cancel))
          .filter { it }
          .subscribe {
            context.startActivity(Intent(context, RegisterPhoneActivity::class.java))
          }
    } else {
      f()
    }
  }
}