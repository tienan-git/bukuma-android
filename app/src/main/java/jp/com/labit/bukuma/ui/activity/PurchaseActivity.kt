package jp.com.labit.bukuma.ui.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.View
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.BukumaError
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.toHtmlSpanned
import jp.com.labit.bukuma.extension.toJson
import jp.com.labit.bukuma.model.Address
import jp.com.labit.bukuma.model.CreditCard
import jp.com.labit.bukuma.model.Merchandise
import jp.com.labit.bukuma.model.realm.User
import jp.com.labit.bukuma.ui.activity.drawer.InviteActivity
import jp.com.labit.bukuma.ui.activity.setting.AddressActivity
import jp.com.labit.bukuma.ui.activity.setting.AddressEditActivity
import jp.com.labit.bukuma.ui.activity.setting.CreditCardEditActivity
import jp.com.labit.bukuma.ui.custom.PicassoCircleTransform
import jp.com.labit.bukuma.ui.dialog.PurchasedDialog
import jp.com.labit.bukuma.util.RxAlertDialog
import jp.com.labit.bukuma.util.infoDialog
import jp.com.labit.bukuma.util.loadingDialog
import jp.com.labit.bukuma.util.shareTextAndLink
import kotlinx.android.synthetic.main.activity_purchase.*
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 10/20/2016 AD.
 * Purchase book activity
 */
class PurchaseActivity : BaseActivity(), PurchasedDialog.PurchasedDialogCallback {

  val REQUEST_ADDRESS = 909
  var merchandiseId = 0
  var transactionId = 0

  lateinit var selectedAddress: Address
  lateinit var selectedMerchandise: Merchandise
  var selectedCreditCard: CreditCard? = null

  companion object {
    val EXTRA_MERCHANDISE_ID = "extra_merchandise_id"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_purchase)
    supportActionBar?.title = getString(R.string.title_purchase)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    merchandiseId = intent.getIntExtra(EXTRA_MERCHANDISE_ID, 0)
    if (merchandiseId == 0) throw IllegalArgumentException("need merchandise id")

    content_layout.visibility = View.GONE

    val infoTextColor = ContextCompat.getColor(this, R.color.black87)
    price_formview.inputView.setTextColor(infoTextColor)
    price_formview.inputView.setTypeface(null, Typeface.BOLD)
    bonus_point_formview.inputView.setTextColor(infoTextColor)
    bonus_point_formview.inputView.setTypeface(null, Typeface.BOLD)
    sale_point_formview.inputView.setTextColor(infoTextColor)
    sale_point_formview.inputView.setTypeface(null, Typeface.BOLD)
    amount_formview.inputView.setTextColor(infoTextColor)
    amount_formview.inputView.setTypeface(null, Typeface.BOLD)

    RxView.clicks(purchase_button).throttleFirst(1, TimeUnit.SECONDS).subscribe { purchase() }
  }

  override fun onResume() {
    super.onResume()
    getData()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_ADDRESS) {
      if (resultCode != Activity.RESULT_OK) finish()
    }
  }

  fun getData() {
    val progress = loadingDialog(this)

    // get default address + updated merchandise
    service.users.refreshUser()
      .observeOn(Schedulers.newThread())
      .flatMap { service.api.getDefaultAddress() }
      .observeOn(AndroidSchedulers.mainThread())
      .doOnError {
        Timber.e(it, "failed load default address")
        val error = BukumaError.errorType(it)
        if (error == BukumaError.Type.HttpError && error.errorCode == 404) {
          // address not found -> ask user to add address
          RxAlertDialog.alert2(this, null,
            getString(R.string.purchase_error_need_address_message),
            getString(R.string.purchase_error_need_address_ok),
            getString(R.string.purchase_error_need_address_cancel))
            .subscribe {
              if (it) {
                val intent = Intent(this, AddressEditActivity::class.java)
                startActivityForResult(intent, REQUEST_ADDRESS)
              } else finish()
            }
        }
      }
      .observeOn(Schedulers.newThread())
      .flatMap {
        Single.zip(Single.just(it), service.api.getMerchandise(merchandiseId)) { f, s -> Pair(f, s) }
      }
      .observeOn(AndroidSchedulers.mainThread())
      .doOnEach { progress.dismiss() }
      .subscribe({
        Timber.i("loaded merchandise")
        setAddress(it.first.address!!)
        setMerchandise(it.second.merchandise!!)
        setAmountInfo(it.second.merchandise!!, service.currentUser!!)
        content_layout.visibility = View.VISIBLE
      }, {
        Timber.e(it, "failed load merchandise")
      })

    // get default credit card
    service.api.getDefaultCreditCard()
      .subscribeOn(Schedulers.newThread())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({
        Timber.i("get credit card success")
        setCreditCard(it.creditCard!!)
      }, {
        Timber.e(it, "get credit card failed")
        setCreditCard(null)
      })
  }

  fun setMerchandise(merchandise: Merchandise) {
    selectedMerchandise = merchandise

    // book info
    title_textview.text = merchandise.title()
    merchandise.book?.let {
      author_textview.text = it.author?.name
      publisher_textview.text = it.publisher?.name

      val lp = book_imageview.layoutParams
      val width = lp.width
      val ratio = width.toFloat() / it.width()
      val height = (it.height() * ratio).toInt()

      lp.height = height
      book_imageview.layoutParams = lp

      val cover = it.coverImageUrl
      if (cover != null) {
        picasso.load(cover).into(book_imageview)
      } else {
        picasso.load(R.drawable.img_book_placeholder).into(book_imageview)
      }
    }

    // user info
    merchandise.user?.let { user ->
      val avatarUrl = user.profileIcon
      if (avatarUrl != null) {
        picasso.load(avatarUrl).transform(PicassoCircleTransform()).into(seller_imageview)
      } else {
        picasso.load(R.drawable.img_thumbnail_user).transform(PicassoCircleTransform()).into(seller_imageview)
      }

      val shipWayArray = resources.getStringArray(R.array.merchandise_shipway_array)
      seller_textview.text = getString(
        R.string.purchase_merchandise_seller,
        user.nickname,
        merchandise.shipFrom,
        merchandise.shipDayFrom(),
        merchandise.shipDayTo(),
        shipWayArray[merchandise.shipMethodIndex()]).toHtmlSpanned()

      RxView.clicks(seller_imageview).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra(ProfileActivity.EXTRA_USER, user.toJson())
        startActivity(intent)
      }
    }
  }

  fun setAmountInfo(merchandise: Merchandise, buyer: User) {
    // amount info
    price_formview.inputView.text = merchandise.priceTextWithShippingIncludedMark(this)

    // points deduct
    val purchasePoint = buyer.purchasePrice(merchandise)
    val bonusUsed = purchasePoint.first
    val pointUsed = purchasePoint.second
    if (bonusUsed > 0) {
      bonus_point_layout.visibility = View.VISIBLE
      bonus_point_formview.text = "${bonusUsed}pt"
    } else {
      bonus_point_layout.visibility = View.GONE
    }
    if (pointUsed > 0) {
      sale_point_layout.visibility = View.VISIBLE
      sale_point_formview.text = getString(R.string.money_unit, pointUsed)
    } else {
      sale_point_layout.visibility = View.GONE
    }

    // amount to pay
    val amount = merchandise.price - bonusUsed - pointUsed
    amount_formview.text = getString(R.string.money_unit, amount)
    creditcard_layout.visibility = if (amount > 0) View.VISIBLE else View.GONE
  }

  fun setAddress(address: Address) {
    selectedAddress = address
    address_textview.text = address.addressText()

    RxView.clicks(address_textview).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      val intent = Intent(this, AddressActivity::class.java)
      startActivity(intent)
    }
  }

  fun setCreditCard(card: CreditCard?) {
    selectedCreditCard = card
    if (card != null) {
      creditcard_number_textview.text = "**** **** **** ${card.last_4}"
      creditcard_expiry_textview.text = getString(
        R.string.payment_creditcard_date,
        card.expMonth,
        card.expYear)

      if (card.info != null && card.info!!.brand == "MasterCard") {
        creditcard_imageview.setImageResource(R.drawable.img_credit_master)
      } else {
        creditcard_imageview.setImageResource(R.drawable.img_credit_visa)
      }
    }
  }

  // purchase flow

  fun purchase() {
    // cant purchase sold item
    if (selectedMerchandise.isSold()) {
      infoDialog(this, getString(R.string.purchase_error_sold_message))
      return
    }

    // cant purchase self item
    if (selectedMerchandise.user?.id ?: 0 == service.currentUser!!.id) {
      infoDialog(this, getString(R.string.purchase_error_self))
      return
    }

    // cant purchase credit card expired
    if (selectedCreditCard?.isExpired() ?: false) {
      infoDialog(this, getString(R.string.payment_creditcard_date_expired))
      return
    }

    val buyer = service.currentUser!!
    val price = selectedMerchandise.price
    val purchasePoint = buyer.purchasePrice(selectedMerchandise)
    val amount = price - purchasePoint.first - purchasePoint.second

    if (amount > 0 && selectedCreditCard == null) {
      // show dialog ask to add credit card
      AlertDialog.Builder(this)
        .setMessage(getString(R.string.purchase_error_insufficient_point_message))
        .setNegativeButton(R.string.purchase_error_insufficient_point_invite, { d, i ->
          startActivity(Intent(this, InviteActivity::class.java))
        })
        .setPositiveButton(R.string.purchase_error_insufficient_point_add_card, { d, i ->
          startActivity(Intent(this, CreditCardEditActivity::class.java))
        })
        .setNeutralButton(R.string.cancel, null)
        .show()
      return
    }

    // message
    val message = if (amount > 0) {
      if (purchasePoint.first > 0 && purchasePoint.second > 0) {
        getString(R.string.purchase_confirm_bonus_normal_credit_message,
          purchasePoint.first,
          purchasePoint.second,
          amount)
      } else if (purchasePoint.first > 0) {
        getString(R.string.purchase_confirm_bonus_credit_message,
          purchasePoint.first,
          amount)
      } else if (purchasePoint.second > 0) {
        getString(R.string.purchase_confirm_normal_credit_message,
          purchasePoint.second,
          amount)
      } else {
        getString(R.string.purchase_confirm_message)
      }
    } else {
      if (purchasePoint.first > 0 && purchasePoint.second > 0) {
        getString(R.string.purchase_confirm_bonus_normal_message,
          purchasePoint.first,
          purchasePoint.second)
      } else if (purchasePoint.first > 0) {
        getString(R.string.purchase_confirm_bonus_message, purchasePoint.first)
      } else if (purchasePoint.second > 0) {
        getString(R.string.purchase_confirm_normal_message, purchasePoint.second)
      } else {
        getString(R.string.purchase_confirm_message)
      }
    }

    // confirm
    val progress = ProgressDialog(this)
    progress.setMessage(getString(R.string.loading))
    progress.setCancelable(false)

    RxAlertDialog.alert2(this, null, message,
      getString(R.string.purchase_confirm_ok),
      getString(R.string.cancel))
      .filter { it }
      .doOnNext { progress.show() }
      .observeOn(Schedulers.newThread())
      .flatMap {
        service.purchaseMerchandise(
          selectedMerchandise,
          selectedAddress.id,
          selectedCreditCard?.id).toObservable()
      }
      .doOnEach { progress.dismiss() }
      .subscribe({
        Timber.i("success purchase merchandise transaction id : $it")
        tracker.trackMerchandisePurchase(selectedMerchandise, true)
        transactionId = it
        // show thank you
        val dialog = PurchasedDialog.newInstance(
          getString(R.string.purchase_success_dialog_title),
          getString(R.string.purchase_success_dialog_message),
          getString(R.string.invite_dialog_title),
          getString(R.string.invite_code_title),
          buyer.inviteCode,
          getString(R.string.invite_copy_text),
          getString(R.string.invite_button_text),
          getString(R.string.purchase_success_dialog_button),
          R.drawable.img_bg_alldone
        )
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "purchased")
      }, {
        Timber.e(it, "failed purchase merchandise")
        tracker.trackMerchandisePurchase(selectedMerchandise, false)
        infoDialog(this, getString(R.string.error_tryagain))
      })
  }

  override fun onInviteButtonClick() {
    // share text with link
    val invitationPoint = config.invitationPoint
    val text = getString(
        R.string.invite_share_text,
        service.currentUser?.inviteCode ?: "",
        invitationPoint)
    val link = "http://hyperurl.co/01o4af"
    shareTextAndLink(this, text, link)
  }

  // purchase dialog button click
  override fun onNextButtonClick() {
    // show transaction activity
    if (transactionId != 0) {
      val intent = Intent(this, TransactionActivity::class.java)
      intent.putExtra(TransactionActivity.EXTRA_TRANSACTION_ID, transactionId)
      startActivity(intent)
    }
    finish()
  }
}
