package jp.com.labit.bukuma.api

import jp.com.labit.bukuma.api.request.DeleteAccountRequest
import jp.com.labit.bukuma.api.response.*
import okhttp3.RequestBody
import retrofit2.http.*
import rx.Single

/**
 * Created by zoonooz on 9/9/2016 AD.
 * All interface for Bukuma api
 */
interface BukumaApi {

  companion object {
    val START_PAGE = 1
  }

  // setting & banners

  @GET("https://banners.bukuma.io/api/apps/{app}")
  fun getAppConfig(@Path("app") app: String): Single<AppConfigResponse>

  @GET("https://banners.bukuma.io/api/apps/{app}/banners")
  fun getAppBanners(@Path("app") app: String): Single<AppBannersResponse>

  // device

  @Multipart
  @POST("v1/devices/new")
  fun sendToken(@Part("device_token") token: String,
                @Part("uuid") uuid: String,
                @Part("device_type") type: String = "android"): Single<BaseResponse>

  @Multipart
  @POST("v1/devices/new")
  fun sendDevice(@Part("uuid") uuid: String,
                 @Part("device_token") token: String?,
                 @Part("advertising_id") advertisingId: String?,
                 @Part("app_version") appVersion: String?,
                 @Part("os_version") osVersion: String?,
                 @Part("device_type") type: String = "android"): Single<BaseResponse>

  // category

  @GET("v1/categories")
  fun getCategories(): Single<GetCategoriesResponse>

  // tab

  @GET("v1/tabs")
  fun getTabs(): Single<GetTabsResponse>

  // book

  @GET("v1/books/{id}")
  fun getBook(@Path("id") bookId: Int): Single<GetBookResponse>

  @GET
  fun getBooks(
      @Url url: String,
      @Query("page") page: Int = START_PAGE): Single<GetBooksResponse>

  @GET("v1/books/timeline_category/{id}")
  fun getBooksTimeline(
      @Path("id") categoryId: Int,
      @Query("page") page: Int = START_PAGE): Single<GetBooksResponse>

  @GET("v1/books/all_categories/{id}")
  fun getBookFromCategory(
      @Path("id") categoryId: Int,
      @Query("page") page: Int = START_PAGE): Single<GetBooksResponse>

  @GET("v1/books/price_timeline")
  fun getBooksUnderPrice(
      @Query("category_id") categoryId: Int,
      @Query("price") price: Int,
      @Query("page") page: Int = START_PAGE): Single<GetBooksResponse>

  @GET("v1/books/published_timeline")
  fun getBooksPublishedInDay(
      @Query("category_id") categoryId: Int,
      @Query("day") price: Int,
      @Query("page") page: Int = START_PAGE): Single<GetBooksResponse>

  @GET("v1/books/bulk_timeline")
  fun getBooksBulk(
      @Query("category_id") categoryId: Int,
      @Query("page") page: Int = START_PAGE): Single<GetBooksResponse>

  @Multipart
  @POST("v1/books/search")
  fun searchBooks(
      @Part("keyword") keyword: String,
      @Query("page") page: Int = START_PAGE): Single<GetBooksResponse>

  @FormUrlEncoded
  @POST("v1/books/suggest")
  fun suggestWords(
      @Field("keyword") keyword: String): Single<GetSuggestWordsResponse>

  @Multipart
  @POST("v1/books/search_orders")
  fun createSearchOrder(@Part("keyword") query: String): Single<GetSearchOrderResponse>

  @GET("v1/books/search_orders/{id}")
  fun getSearchOrder(@Path("id") id: Int): Single<GetSearchOrderResponse>

  @GET("v1/books/liked_books")
  fun getBooksLiked(@Query("page") page: Int = START_PAGE): Single<GetBooksResponse>

  @POST("v1/books/{id}/like")
  fun likeBook(@Path("id") bookId: Int): Single<BaseResponse>

  @POST("v1/books/{id}/unlike")
  fun unlikeBook(@Path("id") bookId: Int): Single<BaseResponse>

  @GET("v1/books/{id}/merchandises")
  fun getMerchandises(
      @Path("id") bookId: Int,
      @Query("page") page: Int = START_PAGE): Single<GetMerchandisesResponse>

  // tags

  @GET("v1/books/{id}/tags")
  fun getTags(
      @Path("id") bookId: Int,
      @Query("number") number: Int,
      @Query("page") page: Int = START_PAGE): Single<GetTagsResponse>

  @GET("v1/books/{id}/tags?all=1")
  fun getTagsAll(
      @Path("id") bookId: Int): Single<GetTagsResponse>


  @FormUrlEncoded
  @POST("v1/books/{id}/tags/new")
  fun createTag(
      @Path("id") bookId: Int,
      @Field("name") name: String): Single<GetTagResponse>

  @POST("v1/books/{bookId}/tags/{tagId}/vote")
  fun voteTag(
      @Path("bookId") bookId: Int,
      @Path("tagId") tagId: Int): Single<BaseResponse>

  @POST("v1/books/{bookId}/tags/{tagId}/unvote")
  fun unvoteTag(
      @Path("bookId") bookId: Int,
      @Path("tagId") tagId: Int): Single<BaseResponse>

  // merchandise

  @GET("v1/merchandises/user/{id}")
  fun getMerchandisesFromUser(
      @Path("id") userId: Int,
      @Query("page") page: Int = START_PAGE,
      @Query("selling") onlySelling: Int = 0): Single<GetMerchandisesResponse>

  @GET("v1/merchandises/{id}")
  fun getMerchandise(@Path("id") merchandiseId: Int): Single<GetMerchandiseResponse>

  @FormUrlEncoded
  @POST("v2/merchandises/{id}/buy")
  fun buyMerchandise(
      @Path("id") merchandiseId: Int,
      @Field("user_address_id") addressId: Int,
      @Field("credit_card_id") creditCardId: Int?,
      @Field("bonus_point") bonusPoint: Int,
      @Field("point") point: Int,
      @Field("credit") credit: Int): Single<BuyMerchandiseResponse>

  @Multipart
  @POST("v1/merchandises/create")
  fun createMerchandise(
      @Part("book_id") bookId: Int,
      @Part("price") price: Int,
      @Part("quality") quality: Int,
      @Part("description") description: String,
      @Part("ship_from") shipFrom: String,
      @Part("ship_in") shipIn: Int,
      @Part("shipping_method") shippingMethod: Int,
      @Part("is_series") isSeries: Int = 0,
      @Part("series_description") seriesDesc: String? = null,
      @Part("image\"; filename=\"image1.jpg") imageOne: RequestBody? = null,
      @Part("image_2\"; filename=\"image2.jpg") imageTwo: RequestBody? = null,
      @Part("image_3\"; filename=\"image3.jpg") imageThree: RequestBody? = null): Single<GetMerchandiseResponse>

  @Multipart
  @POST("v1/merchandises/{id}/update")
  fun updateMerchandise(
      @Path("id") merchandiseId: Int,
      @Part("book_id") bookId: Int,
      @Part("price") price: Int,
      @Part("quality") quality: Int,
      @Part("description") description: String,
      @Part("ship_from") shipFrom: String,
      @Part("ship_in") shipIn: Int,
      @Part("shipping_method") shippingMethod: Int,
      @Part("is_series") isSeries: Int = 0,
      @Part("series_description") seriesDesc: String? = null,
      @Part("image\"; filename=\"image1.jpg") imageOne: RequestBody? = null,
      @Part("image_2\"; filename=\"image2.jpg") imageTwo: RequestBody? = null,
      @Part("image_3\"; filename=\"image3.jpg") imageThree: RequestBody? = null): Single<GetMerchandiseResponse>

  @POST("v1/merchandises/{id}/delete")
  fun deleteMerchandise(@Path("id") id: Int): Single<BaseResponse>

  // transaction

  @GET("v1/item_transactions/badge_status?latest_id=0")
  fun getTransactionCount(): Single<GetTransactionCountResponse>

  @GET("v1/item_transactions")
  fun getTransactions(@Query("page") page: Int = START_PAGE): Single<GetTransactionsResponse>

  @GET("v1/item_transactions?all=1")
  fun getTransactionsAll(): Single<GetTransactionsResponse>

  @GET("v1/item_transactions/myself")
  fun getMyTransactions(@Query("page") page: Int = START_PAGE): Single<GetTransactionsResponse>

  @GET("v2/item_transactions/{id}")
  fun getTransaction(@Path("id") transactionId: Int): Single<GetTransactionResponse>

  @POST("v1/item_transactions/{id}/shipped")
  fun transactionShipped(@Path("id") transactionId: Int): Single<BaseResponse>

  @Multipart
  @POST("v1/item_transactions/{id}/arrived")
  fun transactionArrived(
      @Path("id") transactionId: Int,
      @Part("comment") comment: String,
      @Part("mood") mood: Int): Single<BaseResponse>

  @Multipart
  @POST("v1/item_transactions/{id}/review_buyer")
  fun transactionFinish(
      @Path("id") transactionId: Int,
      @Part("comment") comment: String,
      @Part("mood") mood: Int): Single<BaseResponse>

  @POST("v1/item_transactions/{id}/cancel")
  fun cancelTransaction(@Path("id") transactionId: Int): Single<BaseResponse>

  // point transaction

  @GET("v1/point_transactions")
  fun getPointTransactions(@Query("page") page: Int = START_PAGE): Single<GetPointTransactionsResponse>

  @GET("v1/point_transactions/sum_expiring")
  fun getExpiringPoint(@Query("day") day: Int = 90): Single<GetExpiringPointResponse>

  // announcement

  @GET("v1/announcements")
  fun getAnnouncements(@Query("page") page: Int = START_PAGE): Single<GetAnnouncementResponse>

  @POST("v1/announcements/{id}/read")
  fun markAnnouncementRead(@Path("id") id: Int): Single<BaseResponse>

  @GET("v1/announcements/unread")
  fun getAnnouncementUnreadCount(): Single<GetAnnouncementUnreadResponse>

  // users

  @GET("v1/users/timestamp")
  fun getTimestamp(): Single<GetTimestampResponse>

  @Multipart
  @POST("v1/users/register")
  fun register(
      @Part("api_key") apiKey: String,
      @Part("timestamp") timestamp: String,
      @Part("signed_info") signedInfo: String,
      @Part("uuid") uuid: String,
      @Part("email") email: String,
      @Part("nickname") nickname: String,
      @Part("biography") biography: String,
      @Part("gender") gender: Int?, // 3 - male 2 - female
      @Part("provider") provider: String, // default email, otherwise: facebook
      @Part("password") password: String?,
      @Part("access_token") accessToken: String?, //must send if provider = facebook
      @Part("uid") userId: String?, // if provider = facebook, sends facebook uid
      @Part("invitation_code") inviteCode: String?,
      @Part("profile_icon_url") profileIconUrl: String?, // if provider = facebook, you may need to send avatar url
      @Part("profile_icon\"; filename=\"attachment.jpg") profileIcon: RequestBody?): Single<AuthenUserResponse>

  @Multipart
  @POST("v1/users/sign_in_with_email")
  fun loginWithEmail(
      @Part("email") email: String,
      @Part("password") password: String,
      @Part("api_key") apiKey: String,
      @Part("uuid") uuid: String,
      @Part("timestamp") timestamp: String,
      @Part("signed_info") signedInfo: String): Single<AuthenUserResponse>

  @Multipart
  @POST("v1/users/sign_in_with_sns")
  fun loginWithSns(
      @Part("provider") provider: String,
      @Part("access_token") accessToken: String,
      @Part("api_key") apiKey: String,
      @Part("uuid") uuid: String): Single<AuthenUserResponse>

  @Multipart
  @POST("v1/users/update")
  fun updateUser(
      @Part("email") email: String,
      @Part("nickname") nickname: String,
      @Part("gender") gender: Int?,
      @Part("biography") biography: String?,
      @Part("full_name") fullName: String?,
      @Part("profile_icon\"; filename=\"attachment.jpg") profileIcon: RequestBody?): Single<BaseResponse>

  @Multipart
  @POST("v1/users/change_password")
  fun changePassword(
      @Part("current_password") current: String,
      @Part("new_password") new: String): Single<BaseResponse>

  @Multipart
  @POST("v1/users/request_password")
  fun resetPassword(@Part("email") email: String): Single<BaseResponse>

  @GET("v1/users/{id}")
  fun getUser(@Path("id") userId: Int): Single<GetUserResponse>

  @Multipart
  @POST("v1/users/check_sns_status")
  fun checkIfSnsUserExist(
      @Part("provider") provider: String,
      @Part("uid") userId: String): Single<BaseResponse>

  @HTTP(method = "DELETE", path = "v1/users/destroy", hasBody = true)
  fun deleteAccount(@Body data: DeleteAccountRequest): Single<BaseResponse>

  @GET("v1/users/notifications")
  fun getNotificationSetting(): Single<GetNotificationResponse>

  @Multipart
  @POST("v1/users/notifications")
  fun setNotificationSetting(
      @Part("notification") notification: String,
      @Part("on") isOn: Int): Single<BaseResponse>

  @Multipart
  @POST("v1/users/phone_number")
  fun updatePhoneNumber(@Part("phone_number") number: String): Single<BaseResponse>

  @Multipart
  @POST("v1/users/phone_verify")
  fun verifyPhoneNumber(@Part("code") code: String): Single<BaseResponse>

  @POST("v1/users/call_me_to_verify")
  fun requestCallToVerify(): Single<BaseResponse>

  @GET("v1/reviews/users/{id}?order=reverse")
  fun getReviews(@Path("id") userId: Int, @Query("page") page: Int = START_PAGE): Single<GetReviewsResponse>

  // invite

  @Multipart
  @POST("v1/invites/invite")
  fun updateInviter(@Part("invite_code") inviteCode: String): Single<BaseResponse>

  @GET("v1/invites")
  fun getInvitedCount(): Single<GetInvitedResponse>

  // user block

  @GET("v1/block_users")
  fun getBlockedUsers(@Query("page") page: Int = START_PAGE): Single<GetBlockUsersResponse>

  @POST("v1/block_users/{id}/block")
  fun blockUser(@Path("id") id: Int): Single<BaseResponse>

  @POST("v1/block_users/{id}/unblock")
  fun unblockUser(@Path("id") id: Int): Single<BaseResponse>

  // bank

  @GET("v1/bank_accounts")
  fun getBankAccounts(@Query("page") page: Int = START_PAGE): Single<GetBanksResponse>

  @Multipart
  @POST("v1/bank_accounts/create")
  fun createBankAccount(
      @Part("name") name: String,
      @Part("name_kana") nameKana: String,
      @Part("bank_name") bankName: String,
      @Part("branch") branchCode: String,
      @Part("account_type") accountType: String,
      @Part("number") accountNumber: String): Single<GetBanksResponse>

  @Multipart
  @POST("v1/bank_accounts/{id}")
  fun updateBankAccount(
      @Path("id") id: Int,
      @Part("name") name: String,
      @Part("name_kana") nameKana: String,
      @Part("bank_name") bankName: String,
      @Part("branch") branchCode: String,
      @Part("account_type") accountType: String,
      @Part("number") accountNumber: String): Single<BaseResponse>

  @Multipart
  @POST("v1/bank_accounts/{id}/withdraw")
  fun withdrawToBankAccount(
      @Path("id") id: Int,
      @Part("point") point: Int): Single<BaseResponse>

  // address + postal

  @GET("v1/addresses")
  fun getAddresses(@Query("page") page: Int = START_PAGE): Single<GetAddressesResponse>

  @GET("v1/addresses/default")
  fun getDefaultAddress(): Single<GetAddressResponse>

  @Multipart
  @POST("v1/addresses/create")
  fun createAddress(
      @Part("default") default: Int, // 0 false 1 true
      @Part("name") name: String,
      @Part("postal_code") code: String,
      @Part("prefecture") prefecture: String,
      @Part("country") country: String,
      @Part("city") city: String,
      @Part("address_1") address1: String,
      @Part("address_2") address2: String? = null,
      @Part("person_name") personName: String? = null,
      @Part("person_name_kana") personNameKana: String? = null,
      @Part("telephone") tel: String? = null): Single<BaseResponse>

  @Multipart
  @POST("v1/addresses/{id}")
  fun updateAddress(
      @Path("id") id: Int, @Part("default") default: Int, // 0 false 1 true
      @Part("name") name: String,
      @Part("postal_code") code: String,
      @Part("prefecture") prefecture: String,
      @Part("country") country: String,
      @Part("city") city: String,
      @Part("address_1") address1: String,
      @Part("address_2") address2: String? = null,
      @Part("person_name") personName: String? = null,
      @Part("person_name_kana") personNameKana: String? = null,
      @Part("telephone") tel: String? = null): Single<BaseResponse>

  @DELETE("v1/addresses/{id}")
  fun deleteAddress(@Path("id") id: Int): Single<BaseResponse>

  @GET("v1/postal_codes/{code}")
  fun getPostalInfo(@Path("code") code: String): Single<GetPostalInfoResponse>

  // credit card

  @GET("v1/credit_cards")
  fun getCreditCards(@Query("page") page: Int = START_PAGE): Single<GetCreditCardsResponse>

  @GET("v1/credit_cards/default")
  fun getDefaultCreditCard(): Single<GetCreditCardResponse>

  @Multipart
  @POST("v1/credit_cards/create")
  fun createCreditCard(
      @Part("default") default: Int, // 0 false 1 true
      @Part("external_id") cardId: String,
      @Part("card_token") cardToken: String,
      @Part("exp_year") expYear: Int,
      @Part("exp_month") expMonth: Int,
      @Part("last_4") lastDigit: String,
      @Part("name") name: String): Single<BaseResponse>

  @Multipart
  @POST("v1/credit_cards/{id}")
  fun setCreditCardActive(
      @Path("id") id: Int,
      @Part("default") active: Int): Single<BaseResponse>

  @DELETE("v1/credit_cards/{id}")
  fun deleteCreditCard(@Path("id") id: Int): Single<BaseResponse>

  @Multipart
  @POST("v1/credit_cards/{id}/purchase")
  fun buyPointByCreditCard(
      @Path("id") id: Int,
      @Part("amount") amount: Int,
      @Part("merchandise_id") merchandiseId: Int): Single<BaseResponse>

  // activities

  @GET("v1/activities/badge_status")
  fun getActivityCount(@Query("latest_id") lastId: Int = 0): Single<GetActivityCountResponse>

  @GET("v1/activities")
  fun getActivities(@Query("page") page: Int = START_PAGE): Single<GetActivityResponse>

  // chat room

  @GET("v1/chat_rooms")
  fun getChatrooms(
      @Query("from_timestamp") fromTime: Long? = null): Single<GetChatRoomsResponse>

  @Multipart
  @POST("v1/chat_rooms/new")
  fun createChatroom(@Part("with_user_id") withUserId: Int): Single<CreateChatRoomResponse>

  @GET("v1/chat_rooms/{id}")
  fun getChatroom(@Path("id") roomId: Long): Single<GetChatRoomResponse>

  @GET("v1/chat_rooms/{id}/messages")
  fun getChatMessages(
      @Path("id") roomId: Long,
      @Query("from_id") fromId: Long? = null,
      @Query("to_id") toId: Long? = null): Single<GetChatMessagesResponse>

  @POST("v1/chat_rooms/{roomId}/messages/{messageId}/read")
  fun readMessage(
      @Path("roomId") roomId: Long,
      @Path("messageId") messageId: Long): Single<BaseResponse>

  @Multipart
  @POST("v1/chat_rooms/{id}/messages/new")
  fun createMessage(
      @Path("id") roomId: Long,
      @Part("message_type") type: String,
      @Part("text") text: String? = null,
      @Part("merchandise_id") merchandiseId: Int? = null,
      @Part("item_transaction_id") transactionId: Int? = null,
      @Part("attachment\"; filename=\"attachment.jpg") attachment: RequestBody?): Single<CreateChatMessageResponse>

  @DELETE("v1/chat_rooms/{id}")
  fun deleteChatroom(@Path("id") id: Long): Single<BaseResponse>

  // report

  @Multipart
  @POST("v1/reports/create")
  fun createReport(
      @Part("reportable_id") id: Int,
      @Part("reportable_type") type: String, // 'Book', 'Merchandise', or 'User'
      @Part("reason") reason: String? = null): Single<BaseResponse>

  // check maintenance

  @GET("check_maintenance")
  fun checkMaintenance(): Single<Unit>
}
