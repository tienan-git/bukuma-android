package jp.com.labit.bukuma.api

import android.app.Application
import io.realm.Realm
import jp.com.labit.bukuma.BukumaConfig
import jp.com.labit.bukuma.BukumaPreference
import jp.com.labit.bukuma.api.resource.*
import jp.com.labit.bukuma.api.response.AppBannersResponse
import jp.com.labit.bukuma.api.response.AppConfigResponse
import jp.com.labit.bukuma.extension.realm
import jp.com.labit.bukuma.model.Merchandise
import jp.com.labit.bukuma.model.Transaction
import jp.com.labit.bukuma.model.realm.*
import retrofit2.adapter.rxjava.HttpException
import rx.Observable
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.EOFException
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 9/9/2016 AD.
 * Bukuma service that connect to server and perform logic
 */
class BukumaService(
    val application: Application,
    val api: BukumaApi,
    val config: BukumaConfig,
    preference: BukumaPreference,
    omise: OmiseApi) {

  // resources
  val categories = CategoryApi(this, config)
  val tabs = TabApi(this, config)
  val users = UserApi(this, config, preference)
  val chatRooms = ChatRoomApi(this, config)
  val creditCards = CreditCardApi(this, config, omise)
  val books = BookApi(this, config)
  val devices = DeviceApi(this, config, application, preference)

  /**
   * Get current logged in user (detached realm object)
   */
  val currentUser: User?
    get() {
      val realm = Realm.getDefaultInstance()
      var user = realm.where(User::class.java).findFirst()
      user?.let { user = realm.copyFromRealm(it) }
      realm.close()
      return user
    }

  /**
   * Get app configuration
   *
   * 1. get from server
   * 2. store in preference
   *
   * @return [Single] of [AppConfigResponse]
   */
  fun getAppConfig(): Single<AppConfigResponse> {
    return api.getAppConfig(config.appName)
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess { res ->
          res.app?.settings?.let {
            config.apply {
              // save
              isOnBati = it.isOnBati == "1"
              isMaintenance = it.isMaintenanced == "1"
              maintenanceUrl = it.maintenanceUrl
              ngWords = it.ngWords
              initialPoint = it.initialPoint.toInt()
              invitationPoint = it.invitationPoint.toInt()
              latestVersion = it.latestAndroidVersion.toInt()
              contactForm = it.contactFormMessage
              transferFee = it.transferFee.toInt()
              needTransferFee = it.needTrasferFee.toInt()
              minApplicableAmount = it.minApplicableAmount.toInt()
              maxApplicableAmount = it.maxApplicableAmount.toInt()
              salesCommissionDate = it.salesCommissionDate
              salesCommissionPercent = it.salesCommissionPercent.toInt()
              minPrice = it.minPrice.toInt()
              maxPrice = it.maxPrice.toInt()
            }
          }
        }
  }

  /**
   * Get app ads banners
   *
   * 1. delete all old banners
   * 2. get banners and save
   *
   * @return [Single] of [AppBannersResponse]
   */
  fun getAppBanners(): Single<AppBannersResponse> {
    return api.getAppBanners(config.appName)
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess { res ->
          realm {
            it.where(Banner::class.java).findAll().deleteAllFromRealm()
            it.copyToRealm(res.banners)
          }
        }
  }

  /**
   * メンテナンスかどうかをチェックする.
   *
   * http response code が `503` であれば、メンテナンス中、
   * `200` であればサービス稼働中
   *
   * @return
   */
  fun checkMaintenance(): Single<Unit> {
    return api.checkMaintenance()
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .onErrorReturn { ex ->
          if (ex is EOFException) {
            return@onErrorReturn
          }
          throw ex
        }
  }

  /**
   *
   * 初期データの取得.
   *
   * categories と tabs を取得。
   * 取得できないと画面に何も表示できないので、5秒おきにリトライするようにしている。
   * [HttpURLConnection.HTTP_UNAVAILABLE] はメンテナンス中なのでリトライせずにエラーとして
   * 処理を終了している。
   *
   * @return
   */
  fun getInitialDataIfNeed(): Observable<Unit> {
    val retryHandler = { stream: Observable<out Throwable> ->
      stream.flatMap {
        val httpException = it as? HttpException
        if (httpException != null && httpException.code() == HttpURLConnection.HTTP_UNAVAILABLE) {
          Observable.create<Unit> { it.onError(httpException) }
        } else {
          Observable.timer(5, TimeUnit.SECONDS)
        }
      }
    }

    return Single.merge(
        categories.getCategoriesIfNeed().retryWhen { retryHandler(it) },
        tabs.getTabsIfNeed().retryWhen { retryHandler(it) })
  }

  /**
   * Purchase merchandise
   *
   * 1. refresh user to get latest points
   * 2. purchase point by credit card
   * 3. purchase merchandise
   * 4. create chat room if need
   * 5. send transaction message
   *
   * @param merchandise book merchandise that will be purchased
   * @param addressId user address id for this transaction
   * @param creditCardId credit card id for payment
   * @return [Single] of transaction id
   *
   * @see Merchandise
   */
  fun purchaseMerchandise(
      merchandise: Merchandise,
      addressId: Int,
      creditCardId: Int?): Single<Int> {
    return users.refreshUser()
        .observeOn(Schedulers.newThread())
        .flatMap {
          val price = merchandise.price
          val points = it.user!!.purchasePrice(merchandise)
          val bonus = points.first
          val point = points.second
          val amount = price - bonus - point
          api.buyMerchandise(merchandise.id, addressId, creditCardId, bonus, point, amount)
        }
        .doOnSuccess {
          chatRooms.getChatRoom(it.roomId).subscribe({}, {})
        }
        .map {
          it.itemTransactionId
        }
        .observeOn(AndroidSchedulers.mainThread())
  }

  /**
   * Package sent notify for seller
   *
   * 1. tell server that package shipped
   * 2. create chatroom if need
   * 3. send message
   *
   * @param transactionId id of book transaction that was shipped
   * @param toUserId id of user which to send chat message to notify
   * @return [Single] of [ChatMessage] list that contains just created message
   */
  fun transactionShipped(transactionId: Int, toUserId: Int): Single<List<ChatMessage>> {
    return api.transactionShipped(transactionId)
        .subscribeOn(Schedulers.newThread())
        .flatMap { createChatRoomAndSendTransactionMessage(transactionId,
            toUserId,
            Transaction.STATUS_SELLER_SHIPPED) }
        .observeOn(AndroidSchedulers.mainThread())
  }

  /**
   * Package arrived, buyer send review
   *
   * 1. tell server that package arrived
   * 2. create chatroom if need
   * 3. send message
   *
   * @param transactionId id of book transaction that arrived
   * @param toUserId id of user which to send chat message to notify
   * @param comment review comment from buyer
   * @param mood satisfaction index
   * @return [Single] of [ChatMessage] list that contains just created message
   */
  fun transactionArrived(
      transactionId: Int,
      toUserId: Int,
      comment: String,
      mood: Int): Single<List<ChatMessage>> {
    return api.transactionArrived(transactionId, comment, mood)
        .subscribeOn(Schedulers.newThread())
        .flatMap { createChatRoomAndSendTransactionMessage(transactionId,
            toUserId,
            Transaction.STATUS_ITEM_ARRIVED) }
        .observeOn(AndroidSchedulers.mainThread())
  }

  /**
   * Finish, Seller review buyer
   *
   * 1. seller review buyer
   * 2. create chat room if need
   * 3. send message
   *
   * @param transactionId id of book transaction to finish
   * @param toUserId id of user which to send message to notify
   * @param comment review comment from seller
   * @param mood satisfaction index
   * @return [Single] of [ChatMessage] list that contains just created message
   */
  fun transactionFinish(
      transactionId: Int,
      toUserId: Int,
      comment: String,
      mood: Int): Single<List<ChatMessage>> {
    return api.transactionFinish(transactionId, comment, mood)
        .subscribeOn(Schedulers.newThread())
        .flatMap { createChatRoomAndSendTransactionMessage(transactionId,
            toUserId,
            Transaction.STATUS_FINISHED) }
        .observeOn(AndroidSchedulers.mainThread())
  }

  // util

  private fun createChatRoomAndSendTransactionMessage(
      transactionId: Int,
      toUserId: Int,
      transactionType: String): Single<List<ChatMessage>> {
    return chatRooms.createChatRoom(toUserId).onErrorReturn { ChatRoom() }
        .flatMap {
          if (it.id == 0L) {
            // just return empty list if create room error
            Single.just(emptyList<ChatMessage>())
          } else {
            val message = ChatMessage()
            message.roomId = it.id
            message.messageType = ChatMessage.Companion.Type.STATUS.stringValue
            message.text = transactionType
            message.itemTransaction = ChatTransaction().apply { id = transactionId }

            chatRooms.createChatMessage(message).onErrorReturn { emptyList() }
          }
        }
  }
}
