package jp.com.labit.bukuma

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * Created by zoonooz on 10/6/2016 AD.
 * Share preference
 */
class BukumaPreference(context: Context) {

  val PREF_IS_FIRST_SCAN = "pref_first_scan"
  val PREF_LAST_FCM_TOKEN = "pref_last_fcm_token"
  val PREF_LAST_ACTIVITY_ID = "pref_last_activity_id"

  // ship info default information index
  val PREF_PUBLISH_SHIP_FROM = "pref_publish_ship_form"
  val PREF_PUBLISH_SHIP_WAY = "pref_publish_ship_way"
  val PREF_PUBLISH_SHIP_DAY = "pref_publish_ship_day"
  val PREF_DISPLAYED_TAG_INTRODUCTION = "pref_displayed_tag_introduction"

  val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

  /**
   * Clear all data
   */
  fun clear() = pref.edit().clear().apply()

  /**
   * first open scanner. after first time getting the value it will be false auto
   */
  var isFirstScan: Boolean
    get() {
      val b = pref.getBoolean(PREF_IS_FIRST_SCAN, true)
      isFirstScan = false
      return b
    }
    set(value) = pref.edit().putBoolean(PREF_IS_FIRST_SCAN, value).apply()

  /**
   * Last FCM token
   */
  var lastFcmToken: String
    get() = pref.getString(PREF_LAST_FCM_TOKEN, "")
    set(value) {
      pref.edit().putString(PREF_LAST_FCM_TOKEN, value).commit()
    }

  /**
   * Last read activities
   */
  var lastActivitiesId: Int
    get() = pref.getInt(PREF_LAST_ACTIVITY_ID, 0)
    set(value) {
      pref.edit().putInt(PREF_LAST_ACTIVITY_ID, value).commit()
    }

  // shipping information

  /**
   * Shipping from index
   */
  var shipFrom: Int
    get() = pref.getInt(PREF_PUBLISH_SHIP_FROM, 0)
    set(value) {
      pref.edit().putInt(PREF_PUBLISH_SHIP_FROM, value).commit()
    }

  /**
   * Shipping way index
   */
  var shipWay: Int
    get() = pref.getInt(PREF_PUBLISH_SHIP_WAY, 0)
    set(value) {
      pref.edit().putInt(PREF_PUBLISH_SHIP_WAY, value).commit()
    }

  /**
   * Shipping days index
   */
  var shipDay: Int
    get() = pref.getInt(PREF_PUBLISH_SHIP_DAY, 0)
    set(value) {
      pref.edit().putInt(PREF_PUBLISH_SHIP_DAY, value).commit()
    }

  /**
   * タグ機能の紹介ダイアログを見たかどうか
   */
  var displayedTagIntroduction: Boolean
    get() = pref.getBoolean(PREF_DISPLAYED_TAG_INTRODUCTION, false)
    set(value) {
      pref.edit().putBoolean(PREF_DISPLAYED_TAG_INTRODUCTION, value).commit()
    }
}
