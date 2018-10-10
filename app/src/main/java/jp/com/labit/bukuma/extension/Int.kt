package jp.com.labit.bukuma.extension

/**
 * Created by zoonooz on 11/14/2016 AD.
 * Int extension
 */

/**
 * Format interger to string using comma seperate (xx,xxx)
 *
 * @return formatted string from int
 */
fun Int.toCommaString(): String {
  return String.format("%,d", this)
}