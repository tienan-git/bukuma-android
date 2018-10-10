package jp.com.labit.bukuma.model.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by zoonooz on 9/14/2016 AD.
 * Model that store search history text
 */
open class SearchHistory() : RealmObject() {

  constructor(text: String, time: Long): this() {
    this.text = text
    this.time = time
  }

  @PrimaryKey var text: String? = null
  var time = 0L
}