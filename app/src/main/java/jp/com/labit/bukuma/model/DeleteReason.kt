package jp.com.labit.bukuma.model

/**
 * Created by zukkey on 2017/06/16.
 * Delete reason item
 */
class DeleteReason {
  var id = 0

  companion object {
    // 退会理由の大項目(KEY_NOT_SELL以外は使っていないが念のため残してある)
    val KEY_LOW_STOCK = "low.stock"
    val KEY_NOT_SELL = 20
    val KEY_BUSINESS_UNSTABLE = "business.unstable"
    val KEY_NOT_UNDERSTAND_HOW_TO_USE = "not.understand"
    val KEY_HARD_TO_USE = "hard.to.use"
    val KEY_NOT_USE_ANYMORE = "not.use.anymore"
    val KEY_DISSATISFIED_ADMIN = "dissatisfied.admin"
    val KEY_USE_OTHER_SERVICE = "use.other.service"
    val KEY_OTHERS = "others"

    // 退会理由の小項目
    val KEY_NO_PURPOSE_BOOK = 10
    val KEY_FEW_PRODUCTS = 11
    val KEY_FEW_FAVORITE_CATEGORIES = 12
    val KEY_HIGH_PRICED_BOOK = 13
    val KEY_OTHERS_MINOR1 = 14
    val KEY_USER_TROUBLE = 30
    val KEY_UNKNOWN_PERSON = 31
    val KEY_ATTACHED_BAD_EVALUATION = 32
    val KEY_OTHERS_MINOR2 = 33
    val KEY_EXHIBITION_METHOD = 40
    val KEY_PURCHASE_METHOD = 41
    val KEY_DELIVERY = 42
    val KEY_PRICING = 43
    val KEY_PACKING = 44
    val KEY_OTHERS_MINOR3 = 45
    val KEY_FEW_CHOICES = 50
    val KEY_ANONYMOUS = 51
    val KEY_NOT_SEARCH = 52
    val KEY_MUCH_BUG = 53
    val KEY_OTHERS_MINOR4 = 54
    val KEY_SOLD_OUT = 60
    val KEY_BUSY = 61
    val KEY_LITTLE_CAPACITY = 62
    val KEY_GO_ABROAD = 63
    val KEY_OTHERS_MINOR5 =64
    val KEY_MUCH_NEWS = 70
    val KEY_DISSATISFIED_ADMIN_EXCHANGE = 71
    val KEY_OTHERS_MINOR6 = 72
    val KEY_MERCARI = 80
    val KEY_KAURU = 81
    val KEY_FRIL = 82
    val KEY_RAKUMA = 83
    val KEY_MONOKYUN = 84
    val KEY_OTAMART = 85
    val KEY_OTHERS_MINOR7 = 86
    val KEY_LIMITED_FUNCTION = 90
    val KEY_BORED = 91
    val KEY_OTHERS_MINOR8 = 92
  }
}