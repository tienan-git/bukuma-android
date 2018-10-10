package jp.com.labit.bukuma.model

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.toHtmlSpanned
import jp.com.labit.bukuma.model.realm.User

/**
 * Created by zoonooz on 10/17/2016 AD.
 * Merchandise
 */
class Merchandise {
  var id = 0
  var price = 0
  var quality = 0
  var description = ""
  var shipIn = 0
  var shippingMethod = 0
  var shipFrom = ""
  var active = false
  var imageUrl: String? = null
  var image_2Url: String? = null
  var image_3Url: String? = null
  var seriesDescription: String? = null
  var createdAt = 0L
  var updatedAt = 0L
  var soldAt = 0L
  var user: User? = null
  var boughtBy: User? = null
  var book: Book? = null
  var isBrandNew = false

  // utils

  fun title(): String? {
    return book?.let {
      return if (it.isSeries()) seriesDescription
      else it.title
    }
  }

  fun lintPrice(): Int? {
    return book?.let(Book::storePrice)
  }

  fun shipDayFrom() = when (shipIn) {
    0 -> 1
    1 -> 4
    2 -> 7
    else -> 10
  }

  fun shipDayTo() = when (shipIn) {
    0 -> 3
    1 -> 6
    2 -> 9
    else -> 14
  }

  fun shipDays() = when (shipIn) {
    0 -> 4
    1 -> 7
    2 -> 10
    else -> 15
  }

  fun qualityIndex() = if (quality > 4) QUALITY_UNDEFINED else quality
  fun shipMethodIndex() = if (shippingMethod > 7) 7 else shippingMethod
  fun isSold() = soldAt > 0

  fun shipInText(context: Context): String {
    val shipInArray = context.resources.getStringArray(R.array.publish_shipin_text_array)
    return shipInArray[shipIn]
  }

  fun shipDetailText(context: Context): Spanned {
    val shipWayArray = context.resources.getStringArray(R.array.merchandise_shipway_array)
    return context.getString(
        R.string.book_merchandise_ship_detail,
        shipFrom, shipDayFrom(), shipDayTo(),
        shipWayArray[shipMethodIndex()]).toHtmlSpanned()
  }

  fun qualityText(context: Context): String {
    val qualityArray = context.resources.getStringArray(R.array.book_status_array)
    return context.getString(
        R.string.book_detail_status,
        qualityArray[qualityIndex()])
  }

  fun priceTextWithShippingIncludedMark(context: Context): SpannableStringBuilder {
    val spannable = SpannableStringBuilder()
    spannable.append("*  ")
    spannable.setSpan(
        ImageSpan(context, R.drawable.ic_shipping_included),
        0, 1,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    spannable.append(context.getString(R.string.money_unit, price))
    return spannable
  }

  companion object {
    val QUALITY_GREAT = 0
    val QUALITY_GOOD = 1
    val QUALITY_NORMAL = 2
    val QUALITY_BAD = 3
    val QUALITY_UNDEFINED = 4
  }
}
