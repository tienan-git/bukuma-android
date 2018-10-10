package jp.com.labit.bukuma.extension

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by zoonooz on 10/25/2016 AD.
 * Date extension
 */

/**
 * Format the [Date] object to string
 *
 * @param format format that will be used to format
 * @return formatted string
 */
fun Date.toString(format: String): String {
  return SimpleDateFormat(format, Locale.getDefault()).format(this)
}