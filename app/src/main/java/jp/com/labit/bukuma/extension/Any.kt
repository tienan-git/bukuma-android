package jp.com.labit.bukuma.extension

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import io.realm.RealmObject

/**
 * Created by zoonooz on 9/28/2016 AD.
 * Utils extensions
 */

/**
 * Extension to serialize object which may have realm object as properties
 *
 * @return json string
 */
fun Any.toJson(): String {
  val gson = GsonBuilder().setExclusionStrategies(object : ExclusionStrategy {
    override fun shouldSkipField(f: FieldAttributes): Boolean {
      return f.declaringClass == RealmObject::class.java
    }

    override fun shouldSkipClass(clazz: Class<*>): Boolean {
      return false
    }
  }).create()
  return gson.toJson(this)
}

/**
 * Extension to deserialize realm object
 *
 * @param clazz class of the output object that will be deserialize to
 * @return deserialize object
 */
fun <T> String.toObject(clazz: Class<T>): T? {
  val gson = GsonBuilder().setExclusionStrategies(object : ExclusionStrategy {
    override fun shouldSkipField(f: FieldAttributes): Boolean {
      return f.declaringClass == RealmObject::class.java
    }

    override fun shouldSkipClass(clazz: Class<*>): Boolean {
      return false
    }
  }).create()
  return gson.fromJson(this, clazz)
}
