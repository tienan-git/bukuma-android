package jp.com.labit.bukuma.extension

import io.realm.Realm

/**
 * Simple realm transaction function
 * @param block the block that accept the realm object and do some work
 */
fun realm(block: (Realm) -> Unit) {
  val realm = Realm.getDefaultInstance()
  realm.beginTransaction()
  block(realm)
  realm.commitTransaction()
  realm.close()
}