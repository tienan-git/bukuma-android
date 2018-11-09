package jp.com.labit.bukuma.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.toJson
import jp.com.labit.bukuma.model.Book
import jp.com.labit.bukuma.ui.dialog.BookDialog
import jp.com.labit.bukuma.ui.dialog.InfoDialog
import jp.com.labit.bukuma.ui.dialog.SearchingDialog
import jp.com.labit.bukuma.util.RxAlertDialog
import kotlinx.android.synthetic.main.activity_scanner.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 10/6/2016 AD.
 * Barcode scanner activity
 */
class ScannerActivity : BaseActivity(), BookDialog.BookDialogCallback, InfoDialog.InfoDialogCallback {

  companion object {
    val EXTRA_IS_FROM_SEARCH = "extra_is_from_search"
  }
  private val DIALOG_SEARCHING_TAG = "searching"
  private val DIALOG_BOOK_TAG = "book"
  private val REQUEST_CAMERA_CODE = 0

  private var cameraSource: CameraSource? = null
  private var scannerView: SurfaceView? = null

  private var isFirst: Boolean = false
  private var count: Int = 0
  private var countSearch: Int = 0
  private var isFromSearch: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_scanner)

    isFromSearch = intent.getBooleanExtra(EXTRA_IS_FROM_SEARCH, false)

    initializeCamera()
    scannerView = findViewById(R.id.scannerView) as SurfaceView

    // buttons
    RxView.clicks(close_button).subscribe { finish() }
    RxView.clicks(search_title_button).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      val intent = Intent(this, SearchActivity::class.java)
      startActivity(intent)
    }

    // show dialog if first time
    if (preference.isFirstScan) {
      isFirst = true
      val dialog = InfoDialog.newInstance(
        getString(R.string.scanner_dialog_tutorial_title),
        getString(R.string.scanner_dialog_tutorial_description),
        getString(R.string.scanner_dialog_tutorial_ok),
        R.drawable.img_bg_bq)
        dialog.show(supportFragmentManager, "tutorial")
    }

    // permission
    val permission = Manifest.permission.CAMERA
    if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
      // Should we show an explanation?
      ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_CAMERA_CODE)
    }
  }


  fun initializeCamera() {
    val barcodeDetector = BarcodeDetector
        .Builder(this)
        .setBarcodeFormats(Barcode.EAN_13)
        .build()
    barcodeDetector.setProcessor(object:Detector.Processor<Barcode> {
      override fun release() {
      }
      override fun receiveDetections(detections: Detector.Detections<Barcode>) {
        if(detections.detectedItems.size() != 0)
          runOnUiThread { handleResult(detections.detectedItems.valueAt(0).rawValue) }
      }
    })

    if(!barcodeDetector.isOperational) {
      Timber.i("Detector dependencies are not yet available.")
      return
    }

    cameraSource = CameraSource.Builder(this, barcodeDetector)
        .setRequestedFps(15.0f)
        .setAutoFocusEnabled(true)
        .build()
  }

  override fun onResume() {
    super.onResume()
    if (cameraSource == null) {
      initializeCamera()
    }
    (supportFragmentManager.findFragmentByTag(DIALOG_SEARCHING_TAG) as? DialogFragment)?.dismiss()
    (supportFragmentManager.findFragmentByTag(DIALOG_BOOK_TAG) as? DialogFragment)?.dismiss()

    scannerView?.holder?.addCallback(object:SurfaceHolder.Callback {
      override fun surfaceCreated(holder: SurfaceHolder?) {
        try {
          if(isFirst) {
            isFirst = false
          } else {
            if(ContextCompat.checkSelfPermission(this@ScannerActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
              cameraSource?.start(holder)
            }
          Timber.i("Camera Start !!")
          }
        } catch (e:IOException) {
          Timber.e("Failed to start camera preview", e)
        }
      }

      override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
      }

      override fun surfaceDestroyed(holder: SurfaceHolder?) {
        Timber.i("surfaceDestroyed!!!!")
        releaseCameraAndPreview()
        count = 0
        countSearch = 0
      }
    })
  }

  override fun onPause() {
    super.onPause()
    if(scannerView != null)
      releaseCameraAndPreview()
  }

  override fun onSaveInstanceState(outState: Bundle?) {
    releaseCameraAndPreview()
    Timber.i("onSaveInstanceState!!!!")
    super.onSaveInstanceState(outState)
  }

  fun handleResult(contents: String) {
    val content = contents
    Timber.i("barcode scan : $content")

    if(!contents.startsWith("978") && !contents.startsWith("491")
        && !contents.startsWith("979")) {
      Timber.i("This is not ISBN")
      return
    }

    if(countSearch == 0) {
      countSearch += 1
      val dialog = SearchingDialog()
      dialog.isCancelable = false
      dialog.show(supportFragmentManager, DIALOG_SEARCHING_TAG)


      service.api.searchBooks(content)
          .subscribeOn(Schedulers.newThread())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe({
            Timber.i("isbn search success")
            if (it.books.isEmpty()) beginSearchOrder(content, dialog)
            else {
              dialog.dismiss()
              bookFound(it.books.first())
            }
          }, {
            Timber.e(it, "isbn search error")
            dialog.dismiss()
            bookNotFound(R.string.error_tryagain)
            count = 0
          })
    }
  }

  fun beginSearchOrder(content: String, dialog: SearchingDialog) {
    // try to search with order again
    service.books.searchOrder(content)
        .flatMap { service.api.searchBooks(content).toObservable().subscribeOn(Schedulers.newThread()) }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnEach { dialog.dismiss() }
        .doOnNext {
          if (it.books.isNotEmpty()) bookFound(it.books.first())
          else throw IllegalStateException("not found") // book not found -> onError
        }
        .subscribe({
          Timber.i("isbn search order success")
        }, {
          Timber.e(it, "isbn search order error")
          countSearch += 1
          // book not found
          bookNotFound(R.string.scanner_dialog_not_found)
          count = 0
        })
  }

  fun bookFound(book: Book) {
    if (isFromSearch){
      val intent = Intent(this, BookActivity::class.java)
      intent.putExtra(BookActivity.EXTRA_BOOK, book.toJson())
      startActivity(intent)
    } else {
      val dialog = BookDialog.newInstance(book)
      count += 1
      dialog.isCancelable = false
      if(count == 1) {
        countSearch += 1
        dialog.show(supportFragmentManager, DIALOG_BOOK_TAG)
      }
    }
  }

  fun bookNotFound(stringResId: Int) {
    RxAlertDialog.alert(this, null, getString(stringResId), getString(R.string.ok)).subscribe {
      countSearch = 0
    }
  }

  private fun releaseCameraAndPreview() {
    if (cameraSource != null) {
      Timber.i("Unable to start camera source.")
      cameraSource?.release()
      cameraSource = null
    }
    Timber.i("releaseCameraSource!!!!")
  }

  // First Scan Permission Dialog
  override fun onButtonClick() {
    val intent = Intent(this, ScannerActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    startActivity(intent)
  }

  // book dialog callback
  override fun onBookDialogDismiss() {
    Timber.i("onDialogCallBack!!!!")
    count = 0
    countSearch = 0
  }
}

