package jp.com.labit.bukuma.ui.activity

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.jakewharton.rxbinding.view.RxView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import jp.com.labit.bukuma.R
import kotlinx.android.synthetic.main.activity_imageviewer.*

/**
 * Created by zoonooz on 11/8/2016 AD.
 * ImageViewer activity
 */
class ImageViewerActivity : BaseActivity(), Target {

  companion object {
    val EXTRA_URL = "extra_url"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN)
    setContentView(R.layout.activity_imageviewer)
    RxView.clicks(close_button).subscribe { finish() }
  }

  override fun onResume() {
    super.onResume()
    val url: String? = intent.getStringExtra(EXTRA_URL)
    if (url != null && !url.isEmpty()) {
      picasso.load(url).into(this)
    }
  }

  // picasso

  override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
    imageview.setImageBitmap(bitmap)
    progressbar.visibility = View.INVISIBLE
  }

  override fun onBitmapFailed(errorDrawable: Drawable?) {

  }

  override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

  }
}
