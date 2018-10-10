package jp.com.labit.bukuma.extension

import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import timber.log.Timber

/**
 * Created by zoonooz on 9/23/2016 AD.
 * TextView extension
 */

/**
 * Get rid of link underline in TextView and also handle the event through callback
 * @param callback click event handler
 */
fun TextView.stripUnderline(callback: URLSpanCallback? = null) {
  val s = SpannableString(text)
  val spans = s.getSpans(0, s.length, URLSpan::class.java)
  for (span in spans) {
    val start = s.getSpanStart(span)
    val end = s.getSpanEnd(span)
    s.removeSpan(span)
    val newSpan = URLSpanNoUnderline(span.url, callback)
    s.setSpan(newSpan, start, end, 0)
  }
  text = s
  movementMethod = LinkMovementMethod.getInstance()
}

// interface for handle click event
interface URLSpanCallback {
  fun onLinkClick(link: String)
}

// no underline span class
class URLSpanNoUnderline(url: String, val spanCallback: URLSpanCallback?) : URLSpan(url) {

  override fun updateDrawState(ds: TextPaint) {
    super.updateDrawState(ds)
    ds.isUnderlineText = false
  }

  override fun onClick(widget: View?) {
    Timber.i("Url clicked : $url")
    spanCallback?.onLinkClick(url)
    if (spanCallback == null) super.onClick(widget)
  }
}