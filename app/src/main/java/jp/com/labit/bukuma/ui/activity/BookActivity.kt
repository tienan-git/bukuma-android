package jp.com.labit.bukuma.ui.activity

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.view.KeyEvent
import android.view.View
import com.jakewharton.rxbinding.view.clicks
import com.jakewharton.rxbinding.widget.textChanges
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.toObject
import jp.com.labit.bukuma.model.Book
import jp.com.labit.bukuma.ui.fragment.BookDetailFragment
import jp.com.labit.bukuma.util.hideKeyboard
import jp.com.labit.bukuma.util.showKeyboard
import kotlinx.android.synthetic.main.activity_book.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 10/14/2016 AD.
 * Book activity
 */
class BookActivity : BaseActivity(), Target, BookDetailFragment.Listener {

  lateinit var book: Book

  companion object {
    val EXTRA_BOOK = "extra_book"
  }

  private val detailFragement: BookDetailFragment?
    get() = supportFragmentManager.findFragmentById(R.id.container) as? BookDetailFragment

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_book)
    ActivityCompat.postponeEnterTransition(this)

    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    book = intent.getStringExtra(EXTRA_BOOK)?.toObject(Book::class.java)
        ?: throw IllegalArgumentException("need book json object")

    // display values
    supportActionBar?.title = book.titleText()

    // toolbar color
    book.coverImageUrl?.let {
      picasso.load(it).into(this)
    }

    // fragment
    supportFragmentManager.beginTransaction()
        .replace(R.id.container, BookDetailFragment.newInstance(book))
        .commit()

    // delay transition
    toolbar?.post {
      ActivityCompat.startPostponedEnterTransition(this)
    }

    tag_edittext.setOnKeyPreImeListener { _, keyCode, _ ->
      if (keyCode == KeyEvent.KEYCODE_BACK) {
        onHideKeyboard()
      }
      false
    }

    tag_edittext.textChanges().subscribe {
      send_tags_button.isEnabled = it.isNotEmpty()
    }

    send_tags_button.clicks().throttleFirst(1, TimeUnit.SECONDS).subscribe {
      tag_edittext.commitDefault()
      Observable.from(tag_edittext.selectedRecipients)
          .flatMap { service.api.createTag(book.id, it.displayName).toObservable() }
          .subscribeOn(Schedulers.newThread())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe({}, {
            Timber.e(it, "create tag error")
          }, {
            detailFragement?.onCreatedTag()
          })
      tag_edittext.setText("")
      hideKeyboard(this)
      onHideKeyboard()
    }
  }

  override fun onResume() {
    super.onResume()
    hideKeyboard(this)
    onHideKeyboard()
  }

  // picasso target

  override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
  }

  override fun onBitmapFailed(errorDrawable: Drawable?) {
  }

  override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
    bitmap?.let {
      val palette = Palette.Builder(it).generate()
      val default = ContextCompat.getColor(this, R.color.colorPrimary)
      toolbar.setBackgroundColor(palette.getVibrantColor(default))
    }
  }

  // BookDetailFragment.Listener

  override fun onClickAddTagsButton() {
    edit_tag_view.visibility = View.VISIBLE
    tag_edittext.requestFocus()
    showKeyboard(this, tag_edittext)
  }

  private fun onHideKeyboard() {
    edit_tag_view.visibility = View.GONE
    tag_edittext.clearSelectedChip()
    tag_edittext.setText("")
    detailFragement?.onHideKeyboard()
  }
}
