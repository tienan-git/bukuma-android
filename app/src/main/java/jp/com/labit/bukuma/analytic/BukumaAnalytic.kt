package jp.com.labit.bukuma.analytic

import android.app.Application
import android.content.Intent
import com.appsflyer.AppsFlyerLib
import com.crashlytics.android.answers.*
import jp.com.labit.bukuma.model.Merchandise
import java.math.BigDecimal
import java.util.*

/**
 * Created by zoonooz on 11/16/2016 AD.
 * Analytic center for Bukuma.
 * All tracking method will be here. All events defined by Labit.
 */
class BukumaAnalytic(val context: Application) {

  private val appflyer = AppsFlyerLib.getInstance()
  private val answer = Answers.getInstance()

  /**
   * init all the analytics
   */
  fun start() {
    appflyer.startTracking(context, "4BgcTpAfE2VGkGXFs6VmtG")
  }

  /**
   * Track installation event
   *
   * @param intent Intent object from referrer broadcast receiver
   * @see ReferrerReceiver
   */
  fun trackInstall(intent: Intent?) {
    val referrer = intent?.getStringExtra("referrer")
    if (referrer != null) {
      answer.logCustom(CustomEvent(Event.INSTALL).putCustomAttribute("referrer", referrer))
    }
  }

  /**
   * Track screen view
   *
   * @param name screen name
   */
  fun trackScreenView(name: String) {
    val param = mapOf(Pair("name", name))
    appflyer.trackEvent(context, Event.SCREEN_VIEW, param)
    answer.logContentView(ContentViewEvent().putContentName(name))
  }

  /**
   * Track user register
   *
   * @param method the way that user register. For example "email" or "facebook"
   */
  fun trackAccountRegister(method: String, success: Boolean) {
    appflyer.trackEvent(context, Event.ACCOUNT_REGISTER, null)
    answer.logSignUp(SignUpEvent().putMethod(method).putSuccess(success))
  }

  /**
   * Track user delete
   */
  fun trackAccountDelete() {
    appflyer.trackEvent(context, Event.ACCOUNT_DELETE, null)
    answer.logCustom(CustomEvent(Event.ACCOUNT_DELETE))
  }

  /**
   * Track merchandise create (User sell the book)
   */
  fun trackMerchandiseCreate() {
    appflyer.trackEvent(context, Event.MERCHANDISE_CREATE, null)
    answer.logCustom(CustomEvent(Event.MERCHANDISE_CREATE))
  }

  /**
   * Track merchandise update (User update the selling book description)
   */
  fun trackMerchandiseUpdate() {
    appflyer.trackEvent(context, Event.MERCHANDISE_UPDATE, null)
    answer.logCustom(CustomEvent(Event.MERCHANDISE_UPDATE))
  }

  /**
   * Track merchandise delete (User remove the book that is selling)
   */
  fun trackMerchandiseDelete() {
    appflyer.trackEvent(context, Event.MERCHANDISE_DELETE, null)
    answer.logCustom(CustomEvent(Event.MERCHANDISE_DELETE))
  }

  /**
   * Track merchandise purchase (User buy a book from another user)
   */
  fun trackMerchandisePurchase(merchandise: Merchandise, success: Boolean) {
    appflyer.trackEvent(context, Event.MERCHANDISE_PURCHASE, null)
    answer.logPurchase(PurchaseEvent()
        .putCurrency(Currency.getInstance("JPY"))
        .putItemPrice(BigDecimal.valueOf(merchandise.price.toDouble()))
        .putItemId(merchandise.book?.id?.toString() ?: "unknown")
        .putItemName(merchandise.title() ?: "unknown")
        .putItemType("book")
        .putSuccess(success))
  }

  /**
   * Track transaction shipped (User shipped the book to buyer)
   */
  fun trackTransactionShipped() {
    appflyer.trackEvent(context, Event.TRANSACTION_SHIPPED, null)
    answer.logCustom(CustomEvent(Event.TRANSACTION_SHIPPED))
  }

  /**
   * Track transaction arrived (User got the book from seller)
   */
  fun trackTransactionArrived() {
    appflyer.trackEvent(context, Event.TRANSACTION_ARRIVED, null)
    answer.logCustom(CustomEvent(Event.TRANSACTION_ARRIVED))
  }

  /**
   * Track transaction finished
   */
  fun trackTransactionFinished() {
    appflyer.trackEvent(context, Event.TRANSACTION_FINISHED, null)
    answer.logCustom(CustomEvent(Event.TRANSACTION_FINISHED))
  }

  /**
   * Track book search
   *
   * @param query search text that user used
   */
  fun trackBookSearchTitle(query: String) {
    appflyer.trackEvent(context, Event.BOOK_SEARCH_TITLE, null)
    answer.logSearch(SearchEvent().putQuery(query))
  }

  /**
   * Track chat room create
   */
  fun trackChatRoomCreate() {
    appflyer.trackEvent(context, Event.ROOM_CREATE, null)
    answer.logCustom(CustomEvent(Event.ROOM_CREATE))
  }

  /**
   * Track send chat message (Text)
   */
  fun trackMessageTextSend() {
    appflyer.trackEvent(context, Event.MESSAGE_TEXT_SEND, null)
    answer.logCustom(CustomEvent(Event.MESSAGE_TEXT_SEND))
  }

  /**
   * Track send chat message (Image)
   */
  fun trackMessageImageSend() {
    appflyer.trackEvent(context, Event.MESSAGE_IMAGE_SEND, null)
    answer.logCustom(CustomEvent(Event.MESSAGE_IMAGE_SEND))
  }

  /**
   * Track mentioning book in chat
   */
  fun trackMessageBookMention() {
    appflyer.trackEvent(context, Event.MESSAGE_BOOK_SEND, null)
    answer.logCustom(CustomEvent(Event.MESSAGE_BOOK_SEND))
  }
}
