package jp.com.labit.bukuma

import android.app.Application
import android.content.Context
import android.provider.Settings
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by zoonooz on 9/20/2016 AD.
 * Configuration from server & Constant
 */
class BukumaConfig(application: Application) {

  private val prefFileName = "jp.com.labit.bukuma.config"
  private val pref = application.getSharedPreferences(prefFileName, Context.MODE_PRIVATE)

  private val KEY_LATEST_VERSION = "latest_version"
  private val KEY_IS_MAINTENANCE = "is_maintenance"
  private val KEY_IS_ON_BATI = "is_on_bati"
  private val KEY_NGWORDS = "ng_words"
  private val KEY_MAINTENANCE_URL = "maintenance_url"
  private val KEY_INITIAL_POINT = "initial_point"
  private val KEY_INVITATION_POINT = "invitation_point"
  private val KEY_CONTACTFORM_MESSAGE = "contact_form_message"
  private val KEY_TRANSFER_FEE = "transfer_fee"
  private val KEY_IS_HIDDEN_GOOD_REVIEW_DIALOG = "is_hidden_good_review_dialog"
  private val NEED_TRANSFER_FEE = "need_trasfer_fee"
  private val MIN_APPLICABLE_AMOUNT = "min_applicable_amount"
  private val MAX_APPLICABLE_AMOUNT = "max_applicable_amount"
  private val KEY_SALES_COMMISSION_DATE = "sales_commission_date"
  private val KEY_SALES_COMMISSION_PERCENT = "sales_commission_percent"
  private val KEY_MIN_PRICE = "min_price"
  private val KEY_MAX_PRICE = "max_price"

  /**
   * device uuid
   */
  val uuid: String = Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID)
  val baseUrl = if (BuildConfig.DEBUG) "http://stg.bukuma.io/" else "https://api.bukuma.io/"
  val appName = if (BuildConfig.DEBUG) "bukuma_stg" else "bukuma_from_v1_5"
  val sharedKey = "bukumaZ1"
  val apiKey = "e5c5b31f2df29dbc37d19f1392ef35745ee82c0800ba7c0ee9ab13571809b84a"
  val omiseKey = if (BuildConfig.DEBUG) "pkey_test_54etvz3ds03qtbgmvbj" else "pkey_54q559ummmk8se3e8pw"

  /**
   * Last version code
   */
  var latestVersion: Int
    get() = pref.getInt(KEY_LATEST_VERSION, 0)
    set(value) {
      pref.edit().putInt(KEY_LATEST_VERSION, value).commit()
    }

  /**
   * Currently user for show invitation code
   */
  var isOnBati: Boolean
    get() = pref.getBoolean(KEY_IS_ON_BATI, false)
    set(value) {
      pref.edit().putBoolean(KEY_IS_ON_BATI, value).commit()
    }


  /**
   * Maintenance status
   */
  var isMaintenance: Boolean
    get() = pref.getBoolean(KEY_IS_MAINTENANCE, false)
    set(value) {
      pref.edit().putBoolean(KEY_IS_MAINTENANCE, value).commit()
    }

  /**
   * Ng word (Banned)
   */
  var ngWords: String
    get() = pref.getString(KEY_NGWORDS, "")
    set(value) {
      pref.edit().putString(KEY_NGWORDS, value).commit()
    }

  /**
   * Maintenance url
   */
  var maintenanceUrl: String
    get() = pref.getString(KEY_MAINTENANCE_URL, "")
    set(value) {
      pref.edit().putString(KEY_MAINTENANCE_URL, value).commit()
    }

  /**
   * Initial point
   */
  var initialPoint: Int
    get() = pref.getInt(KEY_INITIAL_POINT, 300)
    set(value) {
      pref.edit().putInt(KEY_INITIAL_POINT, value).commit()
    }

  /**
   * Invitation point
   */
  var invitationPoint: Int
    get() = pref.getInt(KEY_INVITATION_POINT, 300)
    set(value) {
      pref.edit().putInt(KEY_INVITATION_POINT, value).commit()
    }

  /**
   * Set business day in contact form
   */
  var contactForm: String
    get() = pref.getString(KEY_CONTACTFORM_MESSAGE, "")
    set(value) {
      pref.edit().putString(KEY_CONTACTFORM_MESSAGE, value).commit()
    }

  /**
   * 売上金申請手数料
   */
  var transferFee: Int
    get() = pref.getInt(KEY_TRANSFER_FEE, 324)
    set(value) {
      pref.edit().putInt(KEY_TRANSFER_FEE, value).commit()
    }

  /**
   * 手数料必要申請額上限
   */
  var needTransferFee: Int
    get() = pref.getInt(NEED_TRANSFER_FEE, 9999)
    set(value) {
      pref.edit().putInt(NEED_TRANSFER_FEE, value).commit()
    }

  /**
   * 最低申請金額
   */
  var minApplicableAmount: Int
    get() = pref.getInt(MIN_APPLICABLE_AMOUNT, 325)
    set(value) {
      pref.edit().putInt(MIN_APPLICABLE_AMOUNT, value).commit()
    }

  /**
   * 申請可能最高額
   */
  var maxApplicableAmount: Int
    get() = pref.getInt(MAX_APPLICABLE_AMOUNT, 99999)
    set(value) {
      pref.edit().putInt(MAX_APPLICABLE_AMOUNT, value).commit()
    }

  /**
   * ダイアログを今後表示しない時のフラグ
   */
  var isHiddenGoodReviewDialog: Boolean
    get() = pref.getBoolean(KEY_IS_HIDDEN_GOOD_REVIEW_DIALOG, false)
    set(value) {
     pref.edit().putBoolean(KEY_IS_HIDDEN_GOOD_REVIEW_DIALOG, value).commit()
    }

  /**
   * 手数料開始日時
   */
  var salesCommissionDate: String
    get() = pref.getString(KEY_SALES_COMMISSION_DATE, "")
    set(value) {
      pref.edit().putString(KEY_SALES_COMMISSION_DATE, value).commit()
    }

  val salesCommissonStartedAt: Long
    get() {
      val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm")
      formatter.timeZone = TimeZone.getTimeZone("Asia/Tokyo")
      return formatter.parse(salesCommissionDate).time / 1000
    }

  /**
   * 手数料割合(%)
   */
  var salesCommissionPercent: Int
    get() = pref.getInt(KEY_SALES_COMMISSION_PERCENT, 0)
    set(value) {
      pref.edit().putInt(KEY_SALES_COMMISSION_PERCENT, value).commit()
    }

  /**
   * 最低出品価格(¥)
   */
  var minPrice: Int
    get() = pref.getInt(KEY_MIN_PRICE, 0)
    set(value) {
      pref.edit().putInt(KEY_MIN_PRICE, value).commit()
    }

  /**
   * 最高出品価格(¥)
   */
  var maxPrice: Int
    get() = pref.getInt(KEY_MAX_PRICE, 0)
    set(value) {
      pref.edit().putInt(KEY_MAX_PRICE, value).commit()
    }
}
