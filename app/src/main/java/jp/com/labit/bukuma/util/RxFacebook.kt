package jp.com.labit.bukuma.util

import android.os.Bundle
import com.facebook.AccessToken
import com.facebook.GraphRequest
import org.json.JSONException
import rx.Single
import rx.schedulers.Schedulers

/**
 * Created by zoonooz on 9/21/2016 AD.
 * Rx for facebook graph request
 */
class RxFacebook {

  companion object {

    /**
     * Create single stream to get facebook profile. Facebook session must be already authorized
     * @return [Single] of [Profile]
     */
    fun getProfile(): Single<Profile> {
      return Single.fromCallable {
        val accessToken = AccessToken.getCurrentAccessToken()
        val graph = GraphRequest.newMeRequest(accessToken, null)
        val param = Bundle()
        param.putString("fields", "id,name,email,gender,first_name,picture.type(large)")
        graph.parameters = param
        val res = graph.executeAndWait()

        if (res.error != null) {
          throw (res.error.exception)
        }

        val json = res.jsonObject
        if (json != null) {
          val id = json.getString("id")
          val name = json.optString("name", null)
          val email = json.optString("email", null)
          val gender = json.optString("gender", null)
          val firstName = json.optString("first_name", null)
          val picture = json.optJSONObject("picture")?.optJSONObject("data")?.optString("url", null)
          val profile = Profile(id, accessToken.token, name, email, gender, firstName, picture)
          return@fromCallable profile
        } else {
          throw JSONException("cant get json")
        }
      }
      .subscribeOn(Schedulers.newThread())
    }
  }

  /**
   * Class represent the Facebook profile
   */
  data class Profile(
    var id: String,
    var token: String,
    var name: String? = null,
    var email: String? = null,
    var gender: String? = null,
    var firstName: String? = null,
    var picture: String? = null
  )
}
