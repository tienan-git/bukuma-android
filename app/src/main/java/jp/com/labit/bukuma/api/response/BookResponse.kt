package jp.com.labit.bukuma.api.response

import jp.com.labit.bukuma.model.Book
import jp.com.labit.bukuma.model.Merchandise
import jp.com.labit.bukuma.model.Tag

/**
 * Created by zoonooz on 10/13/2016 AD.
 * Responses for book
 */

class GetBookResponse : BaseResponse() {
  var book: Book? = null
}

class GetBooksResponse : BaseResponse() {
  var title = ""
  var books = emptyList<Book>()
}

class GetMerchandiseResponse : BaseResponse() {
  var merchandise: Merchandise? = null
}

class GetMerchandisesResponse : BaseResponse() {
  var merchandises = emptyList<Merchandise>()
}

class BuyMerchandiseResponse : BaseResponse() {
  var itemTransactionId = 0
  var roomId = 0L
}

class GetSearchOrderResponse : BaseResponse() {
  var searchOrder: SearchOrder? = null
}

class SearchOrder {
  var id = 0
  var keyword = ""
  var orderType = "keyword"
  var finished = false
  var updatedAt = 0L
}

class GetSuggestWordsResponse : BaseResponse() {
  var words = emptyList<String>()
}

class GetTagsResponse: BaseResponse() {
  var tags = emptyList<Tag>()
}

class GetTagResponse: BaseResponse() {
  var tag: Tag? = null
}
