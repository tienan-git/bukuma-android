package jp.com.labit.bukuma.ui.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.View
import com.jakewharton.rxbinding.view.RxMenuItem
import com.jakewharton.rxbinding.view.RxView
import com.jakewharton.rxbinding.widget.RxAdapterView
import com.jakewharton.rxbinding.widget.RxTextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.*
import jp.com.labit.bukuma.model.Book
import jp.com.labit.bukuma.model.Merchandise
import jp.com.labit.bukuma.ui.dialog.PublishedDialog
import jp.com.labit.bukuma.util.RxAlertDialog
import jp.com.labit.bukuma.util.infoDialog
import kotlinx.android.synthetic.main.activity_publish.*
import okhttp3.MediaType
import okhttp3.RequestBody
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 10/27/2016 AD.
 * Publish book activity
 */
class PublishActivity : BaseImageChooserActivity(), PublishedDialog.Callback {

  private val STATE_PUBLISH_MODE = "state_publish_mode"
  private val STATE_QUALITY = "state_quality"
  private val STATE_PHOTOS = "state_photos"

  companion object {
    val EXTRA_CREATE_BOOK = "extra_create_book"
    val EXTRA_EDIT_MERCHANDISE = "extra_edit_merchandise" // edit mode
    val EXTRA_FROM_BARCODE = "extra_from_barcode"
  }

  private val MODE_SINGLE = 0
  private val MODE_BULK = 1
  private var publishMode = MODE_SINGLE

  private val MODE_CREATE = 0
  private val MODE_EDIT = 1
  private var mode = MODE_CREATE

  lateinit private var publishBook: Book
  private var editMerchandise: Merchandise? = null

  private var publishQuality = Merchandise.QUALITY_GREAT
  private var currentPhotoIndex = 0

  // three photo
  private var photoPaths = arrayOf<String?>(null, null, null)

  private val isShowSalesCommission: Boolean
    get() {
      val startDate = config.salesCommissonStartedAt
      val now = Date().time / 1000
      return editMerchandise?.let { startDate < it.createdAt } ?: (startDate < now)
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_publish)

    supportActionBar?.title = getString(R.string.title_publish)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    editMerchandise = intent.getStringExtra(EXTRA_EDIT_MERCHANDISE)?.toObject(Merchandise::class.java)
    publishBook = intent.getStringExtra(EXTRA_CREATE_BOOK)?.toObject(Book::class.java)
        ?: editMerchandise?.book
        ?: throw IllegalArgumentException("need book json")

    setMode(
        if (editMerchandise != null) MODE_EDIT else MODE_CREATE,
        if (editMerchandise?.book?.isSeries() ?: false) MODE_BULK else MODE_SINGLE)
    setQuality(Merchandise.QUALITY_GREAT)
    setBook(publishBook)
    editMerchandise?.let { setMerchandise(it) }
    sales_commission_layout.visibility = if (isShowSalesCommission) View.VISIBLE else View.GONE

    // default ship information
    ship_from_formview.spinnerView.setSelection(preference.shipFrom)
    ship_way_formview.spinnerView.setSelection(preference.shipWay)
    ship_in_formview.spinnerView.setSelection(preference.shipDay)

    // actions

    RxAdapterView.itemSelections(ship_from_formview.spinnerView).subscribe { preference.shipFrom = it }
    RxAdapterView.itemSelections(ship_way_formview.spinnerView).subscribe { preference.shipWay = it }
    RxAdapterView.itemSelections(ship_in_formview.spinnerView).subscribe { preference.shipDay = it }

    RxView.clicks(one_book_button).subscribe { setMode(mode, MODE_SINGLE) }
    RxView.clicks(bulk_book_button).subscribe { setMode(mode, MODE_BULK) }
    RxView.clicks(status_great_button).subscribe { setQuality(Merchandise.QUALITY_GREAT) }
    RxView.clicks(status_good_button).subscribe { setQuality(Merchandise.QUALITY_GOOD) }
    RxView.clicks(status_normal_button).subscribe { setQuality(Merchandise.QUALITY_NORMAL) }
    RxView.clicks(status_bad_button).subscribe { setQuality(Merchandise.QUALITY_BAD) }

    RxTextView.textChanges(comment_edittext).subscribe {
      comment_count_textview.text = "${it.length} / 200"
    }

    RxView.clicks(help_price_layout).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      openUrl(getString(R.string.url_price))
    }

    RxView.clicks(help_flow_layout).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      openUrl(getString(R.string.url_flow))
    }

    RxView.clicks(help_delivery_layout).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      openUrl(getString(R.string.url_delivery))
    }

    RxView.clicks(image_one_button).throttleFirst(1, TimeUnit.SECONDS).subscribe { chooseImageForIndex(0) }
    RxView.clicks(image_two_button).throttleFirst(1, TimeUnit.SECONDS).subscribe { chooseImageForIndex(1) }
    RxView.clicks(image_three_button).throttleFirst(1, TimeUnit.SECONDS).subscribe { chooseImageForIndex(2) }

    RxTextView.textChanges(price_formview.inputView).subscribe {
      if (it.toString() == "¥") price_formview.text = "" // remove ¥ if there is no number
      else if (it.contains("¥") && !it.startsWith("¥")) price_formview.text = it.trimStart { it != '¥' }.toString()
      else if (it.isNotEmpty() && !it.startsWith("¥")) price_formview.text = "¥$it" // add ¥
      else if (it.count() > 4 && it[it.count() - 4] != ',') price_formview.text = "¥" + try {
        it.toString().replace("¥", "").replace(",", "").toInt().toCommaString()
      } catch (ex: NumberFormatException) {
        it.toString()
      }
      price_formview.inputView.setSelection(price_formview.text.length)

      if(isShowSalesCommission) {
        if (price_formview.text.isEmpty()){
          sales_commission_textview.text = getText(R.string.publish_book_sales_commission_default_value)
          sales_profit_textview.text = "¥0"
        } else {
          val price = price_formview.text.replace("¥", "").replace(",", "").toInt()
          val commission = Math.floor(price * 0.01 * config.salesCommissionPercent).toInt()
          val profit = price - commission
          sales_commission_textview.text = "¥$commission"
          sales_profit_textview.text = "¥$profit"
        }
      }
    }

    val progress = ProgressDialog(this)
    progress.setMessage(getString(R.string.loading))
    progress.setCancelable(false)
    RxView.clicks(delete_button).throttleFirst(1, TimeUnit.SECONDS)
        .filter { editMerchandise?.id ?: 0 != 0 }
        .flatMap {
          RxAlertDialog.alert2(this, null,
              getString(R.string.publish_delete_confirm_message),
              getString(R.string.publish_delete_confirm_ok),
              getString(R.string.cancel))
        }
        .filter { it }
        .doOnNext { progress.show() }
        .observeOn(Schedulers.newThread())
        .flatMap { service.api.deleteMerchandise(editMerchandise!!.id).toObservable().toResponse() }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnEach { progress.dismiss() }
        .doOnNext {
          if (it is Response.Error) {
            Timber.e(it.error, "error delete merchandise id : ${editMerchandise!!.id}")
            infoDialog(this, getString(R.string.error_tryagain))
          }
        }
        .filter { it is Response.Success }
        .flatMap {
          RxAlertDialog.alert(this, null,
              getString(R.string.publish_delete_success_message),
              getString(R.string.ok)).toObservable()
        }
        .subscribe {
          Timber.i("delete merchandise successful")
          tracker.trackMerchandiseDelete()
          setResult(Activity.RESULT_OK)
          finish()
        }
  }

  override fun onSaveInstanceState(outState: Bundle?) {
    super.onSaveInstanceState(outState)
    outState?.putInt(STATE_PUBLISH_MODE, publishMode)
    outState?.putInt(STATE_QUALITY, publishQuality)
    outState?.putStringArray(STATE_PHOTOS, photoPaths)
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
    super.onRestoreInstanceState(savedInstanceState)
    setMode(mode, savedInstanceState?.getInt(STATE_PUBLISH_MODE) ?: MODE_SINGLE)
    setQuality(savedInstanceState?.getInt(STATE_QUALITY) ?: Merchandise.QUALITY_GREAT)

    val imageViews = arrayOf(image_one_button, image_two_button, image_three_button)
    savedInstanceState?.getStringArray(STATE_PHOTOS)?.forEachIndexed { i, s ->
      s?.let { picasso.load("file:$it").fit().centerCrop().into(imageViews[i]) }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_done, menu)

    // action flow
    val progress = ProgressDialog(this)
    progress.setMessage(getString(R.string.loading))
    progress.setCancelable(false)
    RxMenuItem.clicks(menu.findItem(R.id.done))
        .throttleFirst(1, TimeUnit.SECONDS)
        .filter { validate() }
        .doOnEach { progress.show() }
        .observeOn(Schedulers.newThread())
        .flatMap {
          val price = price_formview.text.replace("¥", "").replace(",", "").toInt()
          val desc = comment_edittext.text.toString()
          val prefecture = ship_from_formview.text
          val shipIn = ship_in_formview.spinnerView.selectedItemPosition
          val shipWay = ship_way_formview.spinnerView.selectedItemPosition
          val seriesDesc = if (bulk_title_formview.text.isBlank()) null else bulk_title_formview.text
          val isSeries = if (publishMode == MODE_BULK) 1 else 0

          fun toBody(path: String?): RequestBody? {
            return path?.let { RequestBody.create(MediaType.parse("image/jpeg"), File(it)) }
          }

          if (mode == MODE_CREATE) {
            val bookId = if (publishBook.isSeries()) publishBook.parentId ?: 0 else publishBook.id
            service.api.createMerchandise(
                bookId,
                price,
                publishQuality,
                desc,
                prefecture,
                shipIn,
                shipWay,
                isSeries,
                seriesDesc,
                toBody(photoPaths[0]), toBody(photoPaths[1]), toBody(photoPaths[2])).toObservable().toResponse()
          } else {
            val bookId = publishBook.id
            service.api.updateMerchandise(
                editMerchandise!!.id,
                bookId,
                price,
                publishQuality,
                desc,
                prefecture,
                shipIn,
                shipWay,
                isSeries,
                seriesDesc,
                toBody(photoPaths[0]), toBody(photoPaths[1]), toBody(photoPaths[2])).toObservable().toResponse()
          }
        }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnEach { progress.dismiss() }
        .doOnNext {
          if (it is Response.Error) {
            Timber.e(it.error, "create/update merchandise failed")
            infoDialog(this, getString(R.string.error_tryagain))
          }
          if (mode == MODE_CREATE) tracker.trackMerchandiseCreate()
          else tracker.trackMerchandiseUpdate()
        }
        .subscribe {
          Timber.i("create/update merchandise finished")
          // show dialog
          val fromBarcode = intent.getBooleanExtra(EXTRA_FROM_BARCODE, false)
          val dialog = PublishedDialog.newInstance(fromBarcode)
          dialog.isCancelable = false
          dialog.show(supportFragmentManager, "published")
        }

    return super.onCreateOptionsMenu(menu)
  }

  fun validate(): Boolean {
    var isOk = true
    if (price_formview.text.isBlank()) {
      isOk = false
      price_formview.error = getString(R.string.publish_error_price_empty)
    } else {
      val price = price_formview.text.replace("¥", "").replace(",", "").toInt()
      if (price < config.minPrice) {
        isOk = false
        price_formview.error = getString(R.string.publish_error_price_below_hundred)
      }
      if (price > config.maxPrice) {
        isOk = false
        price_formview.error = getString(R.string.publish_error_price_too_much)
      }
    }

    if (publishMode == MODE_BULK && bulk_title_formview.text.isBlank()) {
      isOk = false
      bulk_title_formview.error = getString(R.string.publish_error_bulk_title_empty)
    }

    return isOk
  }

  // photo

  fun chooseImageForIndex(index: Int) {
    currentPhotoIndex = index
    chooseImage()
  }

  override fun onGotImage(path: String) {
    Timber.i("chosen image at : $path")
    photoPaths[currentPhotoIndex] = path

    val imageViews = arrayOf(image_one_button, image_two_button, image_three_button)
    picasso.load("file:$path").fit().centerCrop()
        .into(imageViews[currentPhotoIndex])
  }

  // set display value

  fun setBook(book: Book) {
    title_textview.text = book.titleText() ?: ""
    author_textview.text = getString(R.string.publish_author, book.author?.name ?: "").toHtmlSpanned()
    publisher_textview.text = getString(R.string.publish_publisher, book.publisher?.name ?: "").toHtmlSpanned()
    store_price_textview.text = getString(R.string.publish_store_price, book.storePrice).toHtmlSpanned()

    val lp = book_imageview.layoutParams
    val width = lp.width
    val ratio = width.toFloat() / book.imageWidth
    val height = (book.imageHeight * ratio).toInt()

    lp.height = height
    book_imageview.layoutParams = lp

    book.coverImageUrl?.let {
      picasso.load(it).into(book_imageview)
    }
  }

  fun setMerchandise(merchandise: Merchandise) {
    bulk_title_formview.text = merchandise.title() ?: ""
    price_formview.text = "${merchandise.price}"
    comment_edittext.setText(merchandise.description)

    merchandise.imageUrl?.let { picasso.load(it).fit().centerCrop().into(image_one_button) }
    merchandise.image_2Url?.let { picasso.load(it).fit().centerCrop().into(image_two_button) }
    merchandise.image_3Url?.let { picasso.load(it).fit().centerCrop().into(image_three_button) }

    setQuality(merchandise.qualityIndex())

    val prefectures = resources.getStringArray(R.array.prefectures)
    ship_from_formview.spinnerView.setSelection(prefectures.indexOf(merchandise.shipFrom))
    ship_way_formview.spinnerView.setSelection(merchandise.shipMethodIndex())
    ship_in_formview.spinnerView.setSelection(merchandise.shipIn)
  }

  fun setMode(mode: Int, publishMode: Int) {
    this.mode = mode
    this.publishMode = publishMode

    // update menu item
    supportInvalidateOptionsMenu()

    if (mode == MODE_EDIT) {
      publish_mode_layout.visibility = View.GONE
      help_layout.visibility = View.GONE
      delete_button.visibility = View.VISIBLE
    } else {
      publish_mode_layout.visibility = View.VISIBLE
      help_layout.visibility = View.VISIBLE
      delete_button.visibility = View.GONE
    }

    one_book_button.isSelected = publishMode == MODE_SINGLE
    bulk_book_button.isSelected = publishMode == MODE_BULK

    // update ui
    if (publishMode == MODE_BULK) {
      bulk_title_layout.visibility = View.VISIBLE
      image_layout.visibility = View.VISIBLE
    } else {
      bulk_title_layout.visibility = View.GONE
      image_layout.visibility = View.GONE
    }
  }

  fun setQuality(quality: Int) {
    publishQuality = quality
    val qualityButtons = arrayOf(
        status_great_button,
        status_good_button,
        status_normal_button,
        status_bad_button)
    val hints = arrayOf(
        R.string.publish_book_comment_great_hint,
        R.string.publish_book_comment_good_hint,
        R.string.publish_book_comment_normal_hint,
        R.string.publish_book_comment_bad_hint)

    qualityButtons.forEach { it.isSelected = false }
    qualityButtons[quality].isSelected = true
    comment_edittext.hint = getString(hints[quality])
  }

  // publish dialog

  override fun onContinueClick() {
    setResult(Activity.RESULT_OK)
    finish()
  }

  override fun onHomeClick() {
    val intent = Intent(this, MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    startActivity(intent)
  }

  override fun onShareClick() {
    try {
      val bitmap = publishBook.coverImageUrl?.let {
        (book_imageview.drawable as? BitmapDrawable)?.bitmap
      }

      val file = bitmap?.let {
        val file = File(externalCacheDir, "share.png")
        val fOut = FileOutputStream(file)
        it.compress(Bitmap.CompressFormat.PNG, 100, fOut)
        fOut.flush()
        fOut.close()
        file.setReadable(true, false)
        file
      }

      val code = service.currentUser!!.inviteCode ?: ""
      val invitationPoint = config.invitationPoint
      val price = price_formview.text
      val text = getString(R.string.publish_success_share_text, code, invitationPoint, price)
      val intent = Intent(Intent.ACTION_SEND)

      intent.putExtra(Intent.EXTRA_TEXT, text)
      file?.let { intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(it)) }
      intent.type = "image/*"
      startActivity(Intent.createChooser(intent, "Share"))
    } catch (e: Exception) {
      Timber.e(e, "publish share error")
    }
  }

  // utils

  fun openUrl(url: String) {
    val intent = Intent(this, WebActivity::class.java)
    intent.putExtra(WebActivity.EXTRA_URL, url)
    startActivity(intent)
  }
}
