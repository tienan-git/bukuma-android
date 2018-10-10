package jp.com.labit.bukuma

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import jp.com.labit.bukuma.api.response.ErrorResponse
import retrofit2.adapter.rxjava.HttpException

/**
 * Created by zoonooz on 9/27/2016 AD.
 * Error handler
 */
class BukumaError {
  enum class Type(code: Int) {
    UnknownError(0), HttpError(1);

    var value = code
    var errorCode = 0
    var customCode = 0
    var invalidCode = ""

    companion object {
      fun valueOf(value: Int, errorCode: Int = 0, customCode: Int = 0): Type {
        for (type in Type.values()) {
          if (type.value == value) {
            type.errorCode = errorCode
            type.customCode = customCode
            return type
          }
        }
        return UnknownError
      }
      fun valueOf(value: Int, errorCode: Int, invalidCode: String): Type {
        for (type in Type.values()) {
          if (type.value == value) {
            type.errorCode = errorCode
            type.invalidCode = invalidCode
            return type
          }
        }
        return UnknownError
      }
    }
  }

  companion object {
    const val HTTP_ERROR_FORBIDDEN = 403

    fun errorType(throwable: Throwable): Type {
      if (throwable is HttpException) {
        val response = errorResponse(throwable)
        if (response != null && response.errorCode != null) {
          return Type.valueOf(1, throwable.code(), response.errorCode!!)
        } else if (response != null && response.code != null) {
          return Type.valueOf(1, throwable.code(), response.code!!)
        }
        return Type.valueOf(1, throwable.code())
      }

      return Type.UnknownError
    }

    internal fun errorResponse(throwable: HttpException): ErrorResponse? {
      try {
        val json = throwable.response().errorBody().string()
        val gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
        return gson.fromJson(json, ErrorResponse::class.java)
      } catch (ignore: Exception) {
      }
      return null
    }
  }
}
