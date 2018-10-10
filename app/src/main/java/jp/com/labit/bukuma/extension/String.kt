package jp.com.labit.bukuma.extension

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.Patterns

/**
 * Created by zoonooz on 9/20/2016 AD.
 * String extension
 */

/**
 * Check if email string is valid
 * @return true if it is valid, otherwise false
 */
fun String.isValidEmail(): Boolean {
  return isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

/**
 * Convert string to md5 string
 * @return md5 string
 */
fun String.md5(): String {
  try {
    val md = java.security.MessageDigest.getInstance("MD5")
    val array = md.digest(toByteArray())
    val sb = StringBuilder()
    for (anArray in array) {
      sb.append(Integer.toHexString(anArray.toInt() and 0xFF or 0x100).substring(1, 3))
    }
    return sb.toString()
  } catch (e: java.security.NoSuchAlgorithmException) {
  }

  return ""
}

/**
 * Check if string is not katakana
 * @return true if it is not katakana, false otherwise
 */
fun String.isNotKatakana(): Boolean {
  for (c in this) {
    if (Character.UnicodeBlock.of(c) != Character.UnicodeBlock.KATAKANA) return true
  }
  return false
}

fun String.toHtmlSpanned(): Spanned {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    return Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
  } else {
    @Suppress("DEPRECATION")
    return Html.fromHtml(this)
  }
}