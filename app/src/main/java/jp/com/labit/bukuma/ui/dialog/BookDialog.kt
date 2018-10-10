package jp.com.labit.bukuma.ui.dialog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.applyIfVerified
import jp.com.labit.bukuma.extension.toJson
import jp.com.labit.bukuma.extension.toObject
import jp.com.labit.bukuma.model.Book
import jp.com.labit.bukuma.ui.activity.BaseActivity
import jp.com.labit.bukuma.ui.activity.PublishActivity
import jp.com.labit.bukuma.ui.activity.ScannerActivity
import kotlinx.android.synthetic.main.dialog_book.*
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 10/20/2016 AD.
 * Book result dialog after scan
 */
class BookDialog : BaseDialog() {

  lateinit var book: Book

  companion object {
    val ARG_BOOK = "arg_book"
    fun newInstance(book: Book): BookDialog {
      val dialog = BookDialog()
      val args = Bundle()
      args.putString(ARG_BOOK, book.toJson())
      dialog.arguments = args
      return dialog
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    book = arguments.getString(ARG_BOOK).toObject(Book::class.java) ?: throw IllegalArgumentException()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.dialog_book, container)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    title_textview.text = book.titleText()

    // cover
    val lp = cover_imageview.layoutParams
    val width = lp.width
    val ratio = width.toFloat() / book.width()
    val height = (book.height() * ratio).toInt()

    lp.height = height
    cover_imageview.layoutParams = lp

    val cover = book.coverImageUrl
    if (cover != null) {
      picasso.load(cover).into(cover_imageview)
    } else {
      picasso.load(R.drawable.img_book_placeholder).into(cover_imageview)
    }

    scan_button.setOnClickListener {
      (activity as? BookDialogCallback)?.onBookDialogDismiss()
      dismiss()
    }

    RxView.clicks(sell_button).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      applyIfVerified(activity as BaseActivity) {
        // begin selling activity
        val intent = Intent(activity, PublishActivity::class.java)
        intent.putExtra(PublishActivity.EXTRA_CREATE_BOOK, book.toJson())
        intent.putExtra(PublishActivity.EXTRA_FROM_BARCODE, true)
        startActivity(intent)
        dismiss()
      }
    }
  }

  interface BookDialogCallback {
    fun onBookDialogDismiss()
  }
}
