package jp.com.labit.bukuma.util

import android.app.Activity
import android.app.ProgressDialog
import android.content.*
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import jp.com.labit.bukuma.R
import java.net.NetworkInterface
import java.net.SocketException

/**
 * Created by zoonooz on 9/8/2016 AD.
 * utility function
 */

/**
 * convert dp unit to pixel unit
 * @param res resource object
 * @param dp dp unit in float
 * @return pixel int
 */
fun dpToPx(res: Resources, dp: Float): Int {
  return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.displayMetrics).toInt()
}

/**
 * Adjust color darkness
 * @param color color
 * @param factor adjust factor
 * @return new color
 */
fun manipulateColor(color: Int, factor: Float): Int {
  val a = Color.alpha(color)
  val r = Math.round(Color.red(color) * factor)
  val g = Math.round(Color.green(color) * factor)
  val b = Math.round(Color.blue(color) * factor)
  return Color.argb(a, Math.min(r, 255), Math.min(g, 255), Math.min(b, 255))
}

/**
 * Hide keyboard
 * @param activity that keyboard currently showing
 */
fun hideKeyboard(activity: Activity) {
  val view = activity.currentFocus
  if (view != null) {
    val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
  }
}

/**
 * Show keyboard
 * @param activity current running activity
 * @param editText focusing EditText
 */
fun showKeyboard(activity: Activity, editText: EditText) {
  val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
}

/**
 * Open app in PlayStore
 * @param context context
 */
fun openAppInStore(context: Context) {
  val appPackageName = context.packageName
  try {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)))
  } catch (e: ActivityNotFoundException) {
    val appLink = "https://play.google.com/store/apps/details?id=" + appPackageName
    context.startActivity(Intent(Intent.ACTION_VIEW,
        Uri.parse(appLink)))
  }
}

/**
 * Share text to other apps
 * @param context context
 * @param text share text
 * @param link share link
 */
fun shareTextAndLink(context: Context, text: String, link: String) {
  val intent = Intent()
  intent.action = Intent.ACTION_SEND
  intent.type = "text/plain"
  intent.putExtra(Intent.EXTRA_TEXT, text + "\n\n" + link)
  context.startActivity(Intent.createChooser(intent, "Share"))
}

/**
 * Get local ip address
 * @return String ip
 */
fun getLocalIpAddress(): String? {
  try {
    val en = NetworkInterface.getNetworkInterfaces()
    while (en.hasMoreElements()) {
      val intf = en.nextElement()
      val enumIpAddr = intf.inetAddresses
      while (enumIpAddr.hasMoreElements()) {
        val inetAddress = enumIpAddr.nextElement()
        if (!inetAddress.isLoopbackAddress) {
          return inetAddress.hostAddress
        }
      }
    }
  } catch (ex: SocketException) {
  }

  return null
}

// progress dialog, alert dialog

/**
 * Create and show loading [ProgressDialog]
 * @param context context
 * @return [ProgressDialog] dialog object
 */
fun loadingDialog(context: Context): ProgressDialog {
  return ProgressDialog.show(context, null, context.getString(R.string.loading))
}

/**
 * Create and show quick info dialog to user
 * @param context context
 * @param msg string message to show
 */
fun infoDialog(context: Context, msg: String) {
  AlertDialog.Builder(context).setMessage(msg).setPositiveButton(R.string.ok, null).show()
}

/**
 * copy the text to clipboard
 * @param context context
 * @param label User-visible label for the clip data.
 * @param text The actual text in the clip.
 */
fun copyToClipboard(context: Context, label: CharSequence, text: CharSequence) {
  val clipboard = context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
  val clip = ClipData.newPlainText(label, text)
  clipboard.primaryClip = clip
}
