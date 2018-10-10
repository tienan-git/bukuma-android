package jp.com.labit.bukuma.api.resource

import jp.com.labit.bukuma.BukumaConfig
import jp.com.labit.bukuma.BukumaPreference
import jp.com.labit.bukuma.api.BukumaService
import jp.com.labit.bukuma.api.request.DeleteAccountRequest
import jp.com.labit.bukuma.api.response.AuthenUserResponse
import jp.com.labit.bukuma.api.response.BaseResponse
import jp.com.labit.bukuma.api.response.GetUserResponse
import jp.com.labit.bukuma.extension.md5
import jp.com.labit.bukuma.extension.realm
import jp.com.labit.bukuma.model.realm.*
import okhttp3.MediaType
import okhttp3.RequestBody
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.io.File

/**
 * Created by zoonooz on 9/28/2016 AD.
 * User api
 */
class UserApi(val service: BukumaService, val config: BukumaConfig, val preference: BukumaPreference) {

  /**
   * Register user by email
   *
   * 1. get time stamp
   * 2. register
   * 3. get user
   * 4. save
   *
   * @param email register email
   * @param nickname user nickname
   * @param password account password
   * @param gender gender integer
   * @param inviteCode invitation code from another user
   * @param profileFile upload image local path
   * @return [Single] of [Pair] that contains user responses
   *
   * @see AuthenUserResponse
   * @see GetUserResponse
   */
  fun registerUserByEmail(
      email: String,
      nickname: String,
      password: String,
      gender: Int,
      inviteCode: String? = null,
      profileFile: String? = null): Single<Pair<AuthenUserResponse, GetUserResponse>> {
    return service.api.getTimestamp()
        .flatMap {
          val profileBody = if (profileFile != null) {
            RequestBody.create(MediaType.parse("image/jpeg"), File(profileFile))
          } else null

          val uuid = config.uuid
          val signSeed = "${config.apiKey}$uuid${it.time}${config.sharedKey}"
          val signInfo = signSeed.md5()
          Timber.d("register sign info : $signSeed -> $signInfo")
          service.api.register(
              config.apiKey, "${it.time}", signInfo, uuid,
              email, nickname, "よろしくお願いします", gender, "email", password,
              null, null, inviteCode, null, profileBody)
        }
        .flatMap { Single.zip(Single.just(it), service.api.getUser(it.userId)) { a, b -> Pair(a, b) } }
        .doOnSuccess { saveUserAfterLogin(it.second.user!!, it.first.accessToken!!) }
        .doOnSuccess { inviteCode?.let { updateInviter(it) } }
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
  }

  /**
   * Register user by facebook
   *
   * 1. get time stamp
   * 2. register
   * 3. get user
   * 4. save
   *
   * @param email email from facebook
   * @param nickname user nickname
   * @param password account password
   * @param gender gender integer
   * @param accessToken facebook access token
   * @param fbUserId facebook user id
   * @param fbProfileUrl facebook profile image url
   * @param inviteCode invitation code from another user
   * @param profileFile upload image local path
   * @return [Single] of [Pair] that contains user responses
   *
   * @see AuthenUserResponse
   * @see GetUserResponse
   */
  fun registerUserByFacebook(
      email: String,
      nickname: String,
      password: String,
      gender: Int,
      accessToken: String,
      fbUserId: String,
      fbProfileUrl: String? = null,
      inviteCode: String? = null,
      profileFile: String? = null): Single<Pair<AuthenUserResponse, GetUserResponse>> {
    return service.api.getTimestamp()
        .flatMap {
          // credential
          val uuid = config.uuid
          val signSeed = "${config.apiKey}$uuid${it.time}${config.sharedKey}"
          val signInfo = signSeed.md5()
          Timber.d("register from facebook sign info : $signSeed -> $signInfo")

          // profile file
          var fbProfile = fbProfileUrl
          val profileBody = if (profileFile != null) {
            fbProfile = null // use file instead
            RequestBody.create(MediaType.parse("image/jpeg"), File(profileFile))
          } else null

          // call api
          service.api.register(
              config.apiKey, "${it.time}", signInfo, uuid,
              email, nickname, "よろしくお願いします", gender, "facebook", password,
              accessToken, fbUserId, inviteCode, fbProfile, profileBody)
        }
        .flatMap { Single.zip(Single.just(it), service.api.getUser(it.userId)) { a, b -> Pair(a, b) } }
        .doOnSuccess { saveUserAfterLogin(it.second.user!!, it.first.accessToken!!) }
        .doOnSuccess { inviteCode?.let { updateInviter(it) } }
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
  }

  /**
   * Login by email
   *
   * 1. get timestamp
   * 2. login with email + password
   * 3. get user
   * 4. save to local
   *
   * @param email account email address
   * @param password account password
   * @return [Single] of [Pair] that contains user responses
   *
   * @see AuthenUserResponse
   * @see GetUserResponse
   */
  fun loginWithEmail(email: String, password: String): Single<Pair<AuthenUserResponse, GetUserResponse>> {
    return service.api.getTimestamp()
        .flatMap {
          // credential
          val uuid = config.uuid
          val signSeed = "${config.apiKey}$uuid${it.time}${config.sharedKey}"
          val signInfo = signSeed.md5()
          Timber.d("login by email sign info : $signSeed -> $signInfo")

          service.api.loginWithEmail(email, password, config.apiKey, uuid, "${it.time}", signInfo)
        }
        .flatMap { Single.zip(Single.just(it), service.api.getUser(it.userId)) { a, b -> Pair(a, b) } }
        .doOnSuccess { saveUserAfterLogin(it.second.user!!, it.first.accessToken!!) }
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
  }

  /**
   * Login by facebook
   *
   * 1. get timestamp
   * 2. login with token
   * 3. get user
   * 4. save to local
   *
   * @param accessToken facebook access token
   * @return [Single] of [Pair] that contains user responses
   *
   * @see AuthenUserResponse
   * @see GetUserResponse
   */
  fun loginWithFacebook(accessToken: String): Single<Pair<AuthenUserResponse, GetUserResponse>> {
    return service.api.getTimestamp()
        .flatMap {
          Timber.d("login by facebook token : $accessToken")
          service.api.loginWithSns("facebook", accessToken, config.apiKey, config.uuid)
        }
        .flatMap { Single.zip(Single.just(it), service.api.getUser(it.userId)) { a, b -> Pair(a, b) } }
        .doOnSuccess { saveUserAfterLogin(it.second.user!!, it.first.accessToken!!) }
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
  }

  /**
   * Update user
   *
   * 1. update
   * 2. refresh local user
   *
   * @param email register email
   * @param nickname user nickname
   * @param gender gender integer
   * @param bio user biology
   * @param profileFile upload image local path
   * @return [Single] of [GetUserResponse]
   */
  fun updateUser(
      email: String,
      nickname: String,
      gender: Int?,
      bio: String?,
      profileFile: String? = null): Single<GetUserResponse> {
    val profile = if (profileFile != null) {
      RequestBody.create(MediaType.parse("image/jpeg"), File(profileFile))
    } else null
    return service.api.updateUser(email, nickname, gender, bio, null, profile)
        .flatMap { refreshUser() }
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
  }

  /**
   * Refresh user value
   *
   * 1. get latest user data
   * 2. save to local
   *
   * @return [Single] of [GetUserResponse] that contains latest user infomation
   */
  fun refreshUser(): Single<GetUserResponse> {
    return service.api.getUser(service.currentUser!!.id)
        .doOnSuccess { updateUserFromResponse(it.user!!) }
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
  }

  /**
   * Verify phone number by sms
   *
   * 1. send sms code to verify
   * 2. update user data
   *
   * @param code activation number receive from sms
   * @return [Single] of [GetUserResponse]
   */
  fun verifyPhoneNumber(code: String): Single<GetUserResponse> {
    return service.api.verifyPhoneNumber(code)
        .flatMap { refreshUser() }
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
  }

  /**
   * Delete account
   *
   * 1. delete on server
   * 2. delete local data
   *
   * @param choice reason index
   * @param comment comment from user
   * @return [Single] of [BaseResponse] indicating the result
   */
  fun deleteAccount(choice: Int, comment: String? = null): Single<BaseResponse> {
    return service.api.deleteAccount(DeleteAccountRequest(choice, comment))
        .doOnSuccess {
          realm {
            // delete all user data
            it.delete(User::class.java)
            it.delete(ChatMember::class.java)
            it.delete(ChatMerchandise::class.java)
            it.delete(ChatTransaction::class.java)
            it.delete(ChatMessage::class.java)
            it.delete(ChatRoom::class.java)
            it.delete(SearchHistory::class.java)
          }
          // remove all temp pref data
          preference.clear()
        }
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
  }

  /**
   * Block other user
   *
   * 1. block user
   * 2. try to delete chat room with this user
   *
   * @param userId user id that will be blocked
   * @return [Single] of [BaseResponse] indicating the result
   */
  fun block(userId: Int): Single<BaseResponse> {
    return service.api.blockUser(userId)
        .flatMap {
          var roomId: Long? = null
          realm {
            roomId = it.where(ChatRoom::class.java)
                .equalTo("friendId", userId)
                .findFirst()?.id
          }
          if (roomId != null) service.chatRooms.deleteChatRoom(roomId!!).onErrorReturn { BaseResponse() }
          else Single.just(BaseResponse())
        }
  }

  //=============================================================================

  private fun saveUserAfterLogin(user: User, accessToken: String) {
    Timber.d("save user with token : $accessToken")
    user.accessToken = accessToken
    realm {
      it.delete(User::class.java)
      it.copyToRealm(user)
    }

    // send token after login
    service.devices.sendInfo()?.subscribe({
      Timber.i("send device info to server success")
    }, {
      Timber.e(it, "send device info to server failed")
    })
  }

  private fun updateUserFromResponse(user: User) {
    user.accessToken = service.currentUser!!.accessToken
    realm {
      it.copyToRealmOrUpdate(user)
    }
  }

  private fun updateInviter(inviteCode: String) {
    // update invite code and don't care about the response
    if (inviteCode.isNotBlank()) service.api.updateInviter(inviteCode).subscribe({}, {})
  }
}
