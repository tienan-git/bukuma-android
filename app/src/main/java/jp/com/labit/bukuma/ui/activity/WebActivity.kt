package jp.com.labit.bukuma.ui.activity

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.jakewharton.rxbinding.view.RxMenuItem
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.toJson
import kotlinx.android.synthetic.main.activity_web.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber

/**
 * Created by zoonooz on 9/14/2016 AD.
 * Activity for show website
 */
class WebActivity : BaseActivity() {

  companion object {
    val EXTRA_URL = "extra_url"
    val EXTRA_TITLE = "extra_title"
    val EXTRA_SHOW_BUTTON = "extra_button"
    val EXTRA_BUTTON_TITLE = "extra_button_title"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_web)

    val url = intent.extras?.getString(EXTRA_URL)
    if (url == null) {
      finish()
      return
    }

    action_layout.visibility = View.GONE

    // set title
    if (supportActionBar != null) {
      val isShowingButton = intent.extras?.getBoolean(EXTRA_SHOW_BUTTON) ?: false
      supportActionBar?.setDisplayHomeAsUpEnabled(!isShowingButton)
      supportActionBar?.title = intent.extras?.getString(EXTRA_TITLE) ?: ""
    }

    // load url
    webview.setWebViewClient(object : WebViewClient() {
      override fun onPageFinished(view: WebView?, url: String?) {
        Timber.i("page started : $url : ${webview.canGoBack()}")
        updateActionLayout()
      }

      @TargetApi(Build.VERSION_CODES.N)
      override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        Timber.d("web view request : ${request?.url}")

        if (hookMoveInApp(request?.url)) {
          return true
        }
        return false
      }

      override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        Timber.d("web view request : $url")

        val urlString = Uri.parse(url)
        if (hookMoveInApp(urlString)) {
          return true
        }
        return false
      }
    })
    webview.setWebChromeClient(object : WebChromeClient() {
      override fun onProgressChanged(view: WebView?, newProgress: Int) {
        progress_bar.visibility = if (newProgress == 100) View.GONE else View.VISIBLE
        progress_bar.progress = newProgress
      }
    })
    webview.settings.javaScriptEnabled = true
    webview.loadUrl(url)

    RxView.clicks(back_button).subscribe { if (webview.canGoBack()) webview.goBack() }
    RxView.clicks(next_button).subscribe { if (webview.canGoForward()) webview.goForward() }
    RxView.clicks(refresh_button).subscribe { webview.reload() }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val isShowingButton = intent.extras.getBoolean(EXTRA_SHOW_BUTTON, false)
    if (isShowingButton) {
      val closeButton = menu.add(intent.extras.getString(EXTRA_BUTTON_TITLE, getString(R.string.close)))
      closeButton.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

      // click event
      RxMenuItem.clicks(closeButton).subscribe { finish() }
    }
    return super.onCreateOptionsMenu(menu)
  }

  private fun updateActionLayout() {
    if (webview.canGoBack()) {
      action_layout.visibility = View.VISIBLE
      next_button.visibility = if (webview.canGoForward()) View.VISIBLE else View.INVISIBLE
    } else {
      action_layout.visibility = View.GONE
    }
  }

  private fun hookMoveInApp(url: Uri?): Boolean {
    if(url == null) return false
    if(url.scheme != "jp.com.labit.bukuma") return false

    when(url.host) {
      "books" -> {
        val bookId = try { url.pathSegments[0]?.toInt() } catch (e: Exception) { null }
        bookId?.let {
          service.api.getBook(it)
              .subscribeOn(Schedulers.newThread())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe({
                it.book?.let {
                  val intent = Intent(this, BookActivity::class.java)
                  intent.putExtra(BookActivity.EXTRA_BOOK, it.toJson())
                  startActivity(intent)
                }
              }, {})
        }
      }
      "timeline" -> {
        url.getQueryParameter("url")?.let {
          val intent = Intent(this, TimelineActivity::class.java)
          intent.putExtra(TimelineActivity.EXTRA_URL, it)
          url.getQueryParameter("title")?.let {
            intent.putExtra(TimelineActivity.EXTRA_TITLE, it)
          }
          url.getQueryParameter("color")?.let {
            (try { Color.parseColor(it) } catch (e: Exception) { null })?.let {
              intent.putExtra(TimelineActivity.EXTRA_COLOR, it)
            }
          }
          startActivity(intent)
        }
      }
    }

    return true
  }
}