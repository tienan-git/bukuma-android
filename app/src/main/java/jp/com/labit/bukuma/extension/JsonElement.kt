package jp.com.labit.bukuma.extension

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

/**
 * Created by zoonooz on 11/21/2016 AD.
 * Json element ext
 */

/**
 * Get primitive type from key without throwing exception like original
 *
 * @param key Json key
 * @return [JsonPrimitive] or null if not exist
 */
fun JsonObject.getAsPrimitiveOrNull(key: String): JsonPrimitive? {
  try {
    return getAsJsonPrimitive(key)
  } catch (ex: Exception) {
    return null
  }
}

/**
 * Get json object from key without throwing exception like original
 *
 * @param key Json key
 * @return [JsonObject] or null if not exist
 */
fun JsonObject.getAsObjectOrNull(key: String): JsonObject? {
  try {
    return getAsJsonObject(key)
  } catch (ex: Exception) {
    return null
  }
}