package jp.com.labit.bukuma.ui.activity

import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.Response
import jp.com.labit.bukuma.extension.toJson
import jp.com.labit.bukuma.extension.toResponse
import jp.com.labit.bukuma.extension.toString
import jp.com.labit.bukuma.model.Address
import jp.com.labit.bukuma.model.Review
import jp.com.labit.bukuma.model.Transaction
import jp.com.labit.bukuma.model.realm.User
import jp.com.labit.bukuma.ui.activity.drawer.ReportActivity
import jp.com.labit.bukuma.ui.custom.PicassoCircleTransform
import jp.com.labit.bukuma.ui.dialog.BadReviewDialog
import jp.com.labit.bukuma.ui.dialog.GoodReviewDialog
import jp.com.labit.bukuma.util.RxAlertDialog
import jp.com.labit.bukuma.util.copyToClipboard
import jp.com.labit.bukuma.util.infoDialog
import jp.com.labit.bukuma.util.loadingDialog
import kotlinx.android.synthetic.main.activity_transaction.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 10/25/2016 AD.
 * Transaction activity
 */
class TransactionActivity : BaseActivity(), GoodReviewDialog.GoodReviewDialogCallback {

  companion object {
    val EXTRA_TRANSACTION_ID = "extra_transaction_id"
  }

  var transactionId = 0
  var userId: Int = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_transaction)
    supportActionBar?.title = getString(R.string.title_transaction)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    transactionId = intent.getIntExtra(EXTRA_TRANSACTION_ID, 0)
    if (transactionId == 0) throw IllegalArgumentException("need transaction id")

    content_layout.visibility = View.GONE
    notice_report_layout.visibility = View.GONE

    // action

    RxView.clicks(happy_button).subscribe { setSelectedMood(0) }
    RxView.clicks(fair_button).subscribe { setSelectedMood(1) }
    RxView.clicks(sad_button).subscribe { setSelectedMood(2) }

    RxView.clicks(inquiry_button).subscribe {
      startActivity(Intent(this, ReportActivity::class.java))
    }

    RxView.clicks(help_1_layout).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      val intent = Intent(this, WebActivity::class.java)
      intent.putExtra(WebActivity.EXTRA_URL, getString(R.string.url_packaging_method))
      startActivity(intent)
    }

    RxView.clicks(help_2_layout).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      val intent = Intent(this, WebActivity::class.java)
      intent.putExtra(WebActivity.EXTRA_URL, getString(R.string.url_trouble_with_trading))
      startActivity(intent)
    }

    // sent package
    val progress = ProgressDialog(this)
    progress.setMessage(getString(R.string.loading))
    progress.setCancelable(false)
    RxView.clicks(sent_button)
      .throttleFirst(1, TimeUnit.SECONDS)
      .flatMap {
        RxAlertDialog.alert2(this, null,
          getString(R.string.transaction_sent_confirm_message),
          getString(R.string.transaction_sent_confirm_ok),
          getString(R.string.cancel))
      }
      .filter { it }
      .doOnNext { progress.show() }
      .flatMap { service.transactionShipped(transactionId, userId).toObservable().toResponse() }
      .doOnEach { progress.dismiss() }
      .doOnNext {
        if (it is Response.Error) {
          Timber.e(it.error, "shipped notify failed")
          infoDialog(this, getString(R.string.error_tryagain))
        }
      }
      .subscribe {
        Timber.i("shipped notified")
        tracker.trackTransactionShipped()
        finish()
      }
  }

  override fun onResume() {
    super.onResume()

    service.api.getTransaction(transactionId)
      .subscribeOn(Schedulers.newThread())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({
        Timber.i("get transaction success")
        setTransaction(it.itemTransaction!!)
        setMerchandise(it.itemTransaction!!)
        setAddress(it.itemTransaction?.userAddress)
        content_layout.visibility = View.VISIBLE
      }, {
        Timber.e(it, "get transaction failed")
      })
  }

  fun setStep(step: Int) {
    if (step > 4) return
    if (step < 0) {
      stepper.visibility = View.GONE
      step_layout.visibility = View.GONE
      return
    } else {
      stepper.visibility = View.VISIBLE
      step_layout.visibility = View.VISIBLE
    }

    val stepTextViews = arrayOf(
      step_one_textview,
      step_two_textview,
      step_three_textview,
      step_four_textview)

    stepper.setCurrentStep(step)
    for (tv in stepTextViews) {
      tv.setTextColor(ContextCompat.getColor(this, R.color.black54))
    }
    stepTextViews[step].setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
  }

  fun setTransaction(transaction: Transaction) {
    val currentUser = service.currentUser!!
    val isBuyer = transaction.user?.id ?: 0 == currentUser.id

    // user
    setUser(if (isBuyer) transaction.seller else transaction.user)

    // cancel header
    if (transaction.cancellable(Date().time) && isBuyer) {
      cancel_layout.visibility = View.VISIBLE

      RxView.clicks(cancel_button).throttleFirst(1, TimeUnit.SECONDS)
        .flatMap {
          RxAlertDialog.alert3(this,
            getString(R.string.transaction_cancel_confirm_dialog_title),
            arrayOf(
              getString(R.string.transaction_cancel_confirm_dialog_ok_button),
              getString(R.string.transaction_cancel_confirm_dialog_contact_button),
              getString(R.string.transaction_cancel_confirm_dialog_cancel_button))).toObservable()
        }
        .subscribe {
          if (it == 0) {
            // cancel transaction
            val progress = loadingDialog(this)
            service.api.cancelTransaction(transaction.id)
              .subscribeOn(Schedulers.newThread())
              .observeOn(AndroidSchedulers.mainThread())
              .doOnEach { progress.dismiss() }
              .flatMap {
                RxAlertDialog.alert(this,
                  null,
                  getString(R.string.transaction_cancel_success_message),
                  getString(R.string.ok))
              }
              .subscribe({
                Timber.i("cancelled transaction id : ${transaction.id}")
                finish()
              }, {
                Timber.e(it, "cancelled transaction failed id : ${transaction.id}")
                infoDialog(this, getString(R.string.error_tryagain))
              })
          } else if (it == 1) {
            // contact form
            val intent = Intent(this, ReportActivity::class.java)
            intent.putExtra(ReportActivity.EXTRA_TRANSACTION_ID, transaction.id)
            intent.putExtra(ReportActivity.EXTRA_TRANSACTION_CODE, transaction.uniqueId)
            startActivity(intent)
          }
        }
    } else {
      cancel_layout.visibility = View.GONE
    }

    // code
    id_formview.text = transaction.uniqueId
    RxView.clicks(id_click_view)
      .throttleFirst(1, TimeUnit.SECONDS)
      .flatMap {
        RxAlertDialog.alertN(this,
          getString(R.string.transaction_id_dialog_title),
          arrayOf(
            getString(R.string.transaction_id_dialog_report_button),
            getString(R.string.transaction_id_dialog_copy_button)),
          getString(R.string.cancel)).toObservable()
      }
      .subscribe {
        if (it == 0) {
          val intent = Intent(this, ReportActivity::class.java)
          intent.putExtra(ReportActivity.EXTRA_TRANSACTION_ID, transactionId)
          intent.putExtra(ReportActivity.EXTRA_TRANSACTION_CODE, transaction.uniqueId)
          startActivity(intent)
        } else if (it == 1) {
          val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
          val clip = ClipData.newPlainText("transaction", transaction.uniqueId)
          clipboard.primaryClip = clip
          infoDialog(this, getString(R.string.transaction_id_copied))
        }
      }

    // reviews
    if (isBuyer) review_button.text = getString(R.string.transaction_review_buyer_button)
    else review_button.text = getString(R.string.transaction_review_seller_button)
    review_layout.visibility = View.GONE
    if ((isBuyer && transaction.status == Transaction.STATUS_SELLER_SHIPPED)
      || !isBuyer && transaction.status == Transaction.STATUS_ITEM_ARRIVED) {
      review_layout.visibility = View.VISIBLE
      setSelectedMood(0)
      RxView.clicks(review_button).throttleFirst(1, TimeUnit.SECONDS)
        .subscribe {
          val mood =
            if (happy_button.isSelected) Review.USER_MOOD_POSITIVE
            else if (fair_button.isSelected) Review.USER_MOOD_SOSO
            else Review.USER_MOOD_NEGATIVE
          review(transaction, isBuyer, mood, review_edittext.text.toString())
        }
    }

    // texts
    chat_textview.text =
      if (isBuyer) getString(R.string.transaction_buyer_chat)
      else getString(R.string.transaction_seller_chat)

    // hide sent button
    sent_button.visibility = View.GONE

    when (transaction.status) {
      Transaction.STATUS_INITIAL -> {
        status_title_textview.text = getString(R.string.transaction_progress_seller_initial_title)
        status_description_textview.text = getString(R.string.transaction_progress_seller_initial_description)
        setStep(0)
      }
      Transaction.STATUS_SELLER_PREPARE -> {
        if (isBuyer) {
          status_title_textview.text = getString(R.string.transaction_progress_buyer_prepare_title)
          status_description_textview.text = getString(R.string.transaction_progress_buyer_prepare_description)
        } else {
          status_title_textview.text = getString(R.string.transaction_progress_seller_prepare_title)
          status_description_textview.text = getString(R.string.transaction_progress_seller_prepare_description)
          sent_button.visibility = View.VISIBLE
        }
        setStep(0)
      }
      Transaction.STATUS_SELLER_SHIPPED -> {
        if (isBuyer) {
          status_title_textview.text = getString(R.string.transaction_progress_buyer_shipped_title)
          status_description_textview.text = getString(R.string.transaction_progress_buyer_shipped_description)
        } else {
          status_title_textview.text = getString(R.string.transaction_progress_seller_shipped_title)
          status_description_textview.text = getString(R.string.transaction_progress_seller_shipped_description)
        }
        setStep(2)
      }
      Transaction.STATUS_ITEM_ARRIVED -> {
        if (isBuyer) {
          status_title_textview.text = getString(R.string.transaction_progress_buyer_arrived_title)
          status_description_textview.text = getString(R.string.transaction_progress_buyer_arrived_description)
        } else {
          status_title_textview.text = getString(R.string.transaction_progress_seller_arrived_title)
          status_description_textview.text = getString(R.string.transaction_progress_seller_arrived_description)
        }
        setStep(3)
      }
      Transaction.STATUS_FINISHED -> {
        if (isBuyer) {
          status_title_textview.text = getString(R.string.transaction_progress_buyer_finished_title)
          status_description_textview.text = getString(R.string.transaction_progress_buyer_finished_description)
        } else {
          status_title_textview.text = getString(R.string.transaction_progress_seller_finished_title)
          status_description_textview.text = getString(R.string.transaction_progress_seller_finished_description)
        }
        if (transaction.afterThreeDays(Date().time)) {
          address_info.visibility = View.GONE
          address_textview.visibility = View.GONE
        }
        setStep(3)
      }
      Transaction.STATUS_PENDING_STAFF -> {
        status_title_textview.text = getString(R.string.transaction_progress_staff_title)
        status_description_textview.text = getString(R.string.transaction_progress_staff_description)
        setStep(-1)
      }
      Transaction.STATUS_CANCELLED -> {
        status_title_textview.text = getString(R.string.transaction_progress_cancelled_title)
        status_description_textview.text = getString(R.string.transaction_progress_cancelled_description)
        address_info.visibility = View.GONE
        address_textview.visibility = View.GONE
        setStep(-1)
      }
    }
  }

  fun setUser(user: User?) {
    if (user != null) {
      userId = user.id
      user_layout.visibility = View.VISIBLE

      name_textview.text = user.nickname
      mood_happy_textview.text = "${user.moodPositive}"
      mood_normal_textview.text = "${user.moodSoso}"
      mood_sad_textview.text = "${user.moodNegative}"

      val avatarUrl = user.profileIcon
      if (avatarUrl != null) {
        picasso.load(avatarUrl).fit().centerCrop().transform(PicassoCircleTransform()).into(avatar_imageview)
      } else {
        picasso.load(R.drawable.img_thumbnail_user).transform(PicassoCircleTransform()).into(avatar_imageview)
      }

      RxView.clicks(seller_layout).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra(ProfileActivity.EXTRA_USER, user.toJson())
        startActivity(intent)
      }

      RxView.clicks(chat_layout).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        // chat
        service.chatRooms.createChatRoom(user.id).subscribe({
          Timber.i("success create chat with : ${user.id}")
          val intent = Intent(this, ChatActivity::class.java)
          intent.putExtra(ChatActivity.EXTRA_ROOM_ID, it.id)
          startActivity(intent)
        }, {
          Timber.e(it, "fail create chat with : ${user.id}")
        })
      }
    } else {
      user_layout.visibility = View.GONE
    }
  }

  fun setMerchandise(transaction: Transaction) {
    val merchandise = transaction.merchandise
    val currentUser = service.currentUser!!
    val isSeller = transaction.seller?.id ?: 0 == currentUser.id

    if (merchandise != null) {
      info_layout.visibility = View.VISIBLE

      // book info
      title_textview.text = merchandise.title()
      val book = merchandise.book
      if (book != null) {
        book_layout.visibility = View.VISIBLE
        author_textview.text = book.author?.name
        publisher_textview.text = book.publisher?.name
        shipping_textview.text = merchandise.shipDetailText(this)
        status_textview.text = merchandise.qualityText(this)

        val lp = book_imageview.layoutParams
        val width = lp.width
        val ratio = width.toFloat() / book.width()
        val height = (book.height() * ratio).toInt()

        lp.height = height
        book_imageview.layoutParams = lp

        val cover = book.coverImageUrl
        if (cover != null) {
          picasso.load(cover).into(book_imageview)
        } else {
          picasso.load(R.drawable.img_book_placeholder).into(book_imageview)
        }

        RxView.clicks(book_layout).throttleFirst(1, TimeUnit.SECONDS).subscribe {
          service.api.getBook(book.id)
            .subscribeOn(Schedulers.newThread())
            .subscribe({
              it.book?.let {
                intent = Intent(this, BookActivity::class.java)
                intent?.putExtra(BookActivity.EXTRA_BOOK, it.toJson())
                startActivity(intent)
              }
            }, {})
        }
      } else {
        book_layout.visibility = View.GONE
      }

      val infoTextColor = ContextCompat.getColor(this, R.color.black87)
      bought_formview.inputView.setTextColor(infoTextColor)
      id_formview.inputView.setTextColor(infoTextColor)

      bought_formview.text = Date(merchandise.soldAt * 1000).toString("yyyy年MM月dd日 HH:mm")
      shipping_date_textview.text = Date((merchandise.soldAt * 1000)
        + (merchandise.shipDayTo() * 86400000)).toString("yyyy年MM月dd日頃")
      ship_in_textview.text = getString(R.string.transaction_ship_in, merchandise.shipInText(this))

      if (isSeller && config.salesCommissonStartedAt < merchandise.createdAt) {
        price_formview.visibility = View.GONE
        price_with_commission_layout.visibility = View.VISIBLE

        val price = merchandise.price
        val commission = Math.floor(price * 0.01 * config.salesCommissionPercent).toInt()
        val profit = price - commission

        price_textview.text = merchandise.priceTextWithShippingIncludedMark(this)
        sales_commission_textview.text = "¥$commission"
        sales_profit_textview.text = "¥$profit"
      } else {
        price_formview.visibility = View.VISIBLE
        price_with_commission_layout.visibility = View.GONE

        price_formview.inputView.setTextColor(infoTextColor)
        price_formview.inputView.text = merchandise.priceTextWithShippingIncludedMark(this)
      }
    } else {
      info_layout.visibility = View.GONE
    }
  }

  fun setAddress(address: Address?) {
    if (address != null) {
      address_layout.visibility = View.VISIBLE
      address_textview.text = address.addressOnlyText()
      buyer_name_textview.text = address.nameText()

      RxView.longClicks(address_text_layout).subscribe {
        copyToClipboard(this, "address", "${address.addressOnlyText()}\n${address.nameText()}")
        infoDialog(this, getString(R.string.transaction_id_copied))
      }
    } else {
      address_layout.visibility = View.GONE
    }
  }

  // review

  fun setSelectedMood(index: Int) {
    val moodHints = arrayOf(
      getString(R.string.transaction_review_good_placeholder),
      getString(R.string.transaction_review_fair_placeholder),
      getString(R.string.transaction_review_bad_placeholder))
    val moodButtons = arrayOf(happy_button, fair_button, sad_button)
    moodButtons.forEach { it.isSelected = false }
    moodButtons[index].isSelected = true
    review_edittext.hint = moodHints[index]
    if (index == 0) {
      if (notice_report_layout.visibility == View.VISIBLE) {
        notice_report_layout.visibility = View.GONE
      }
    } else {
      if (notice_report_layout.visibility == View.GONE) {
        notice_report_layout.visibility = View.VISIBLE
        notice_report_layout.alpha = 0f
        notice_report_layout.animate().alpha(1f).setDuration(300).start()
      }
    }
  }

  fun review(transaction: Transaction, reviewSeller: Boolean, mood: Int, comment: String) {
    if (comment.isBlank()) {
      infoDialog(this, getString(R.string.transaction_review_error_comment_empty))
      return
    }

    val observable =
      if (reviewSeller) service.transactionArrived(transaction.id, userId, comment, mood)
      else service.transactionFinish(transaction.id, userId, comment, mood)

    val progress = loadingDialog(this)
    observable
      .subscribeOn(Schedulers.newThread())
      .observeOn(AndroidSchedulers.mainThread())
      .doOnEach { progress.dismiss() }
      .subscribe({
        Timber.i("transaction review success")
        if (reviewSeller) tracker.trackTransactionArrived()
        else tracker.trackTransactionFinished()
        if (mood == Review.USER_MOOD_POSITIVE && !config.isHiddenGoodReviewDialog) {
          val goodDialog = GoodReviewDialog.newInstance(
            getString(R.string.invite_finished_good_title),
            getString(R.string.invite_description),
            getString(R.string.invite_review_title),
            getString(R.string.invite_review_desc),
            getString(R.string.invite_review_button),
            getString(R.string.invite_none_button),
            getString(R.string.invite_late_button),
            R.drawable.img_bg_alldone
          )
          goodDialog.isCancelable = false
          goodDialog.show(supportFragmentManager, "good")
        } else if (mood == Review.USER_MOOD_POSITIVE && config.isHiddenGoodReviewDialog) {
          val badDialog = BadReviewDialog.newInstance(
            getString(R.string.invite_finished_good_title),
            getString(R.string.invite_description),
            getString(R.string.invite_back_home),
            R.drawable.img_bg_alldone
          )
          badDialog.isCancelable = false
          badDialog.show(supportFragmentManager, "bad")
        } else if (mood == Review.USER_MOOD_SOSO || mood == Review.USER_MOOD_NEGATIVE){
          val badDialog = BadReviewDialog.newInstance(
            getString(R.string.invite_finished_title),
            getString(R.string.invite_description),
            getString(R.string.invite_back_home),
            R.drawable.img_bg_alldone
          )
          badDialog.isCancelable = false
          badDialog.show(supportFragmentManager, "bad")
        }
      }, {
        Timber.e(it, "transaction review failed")
        infoDialog(this, getString(R.string.error_tryagain))
      })
  }

  override fun onNoneButtonClick() {
    config.isHiddenGoodReviewDialog = true
    // show transaction activity
    if (transactionId != 0) {
      val intent = Intent(this, TransactionActivity::class.java)
      intent.putExtra(TransactionActivity.EXTRA_TRANSACTION_ID, transactionId)
      startActivity(intent)
    }
    finish()
  }

  override fun onLateButtonClick() {
    // show transaction activity
    if (transactionId != 0) {
      val intent = Intent(this, TransactionActivity::class.java)
      intent.putExtra(TransactionActivity.EXTRA_TRANSACTION_ID, transactionId)
      startActivity(intent)
    }
    finish()
  }
}
