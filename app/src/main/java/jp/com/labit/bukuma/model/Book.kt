package jp.com.labit.bukuma.model

import jp.com.labit.bukuma.model.realm.Category
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by zoonooz on 9/16/2016 AD.
 * Book
 */
open class Book {
  var id = 0
  var title: String? = null
  var titleEn: String? = null
  var coverImageUrl: String? = null
  var publishedAt: String? = null
  var isbn: String? = null
  var isbn10: String? = null
  var cachedVotesTotal = 0
  var itemsCount = 0
  var edition: String? = null
  var summary: String? = null
  var summaryEn: String? = null
  var bookAuthorsCount: Int? = null
  var fullTitle: String? = null
  var language: String? = null
  var seriesNo: String? = null
  var parentId: Int? = null
  var amazonLink: String? = null
  var rakutenLink: String? = null
  var lastFetchVia: String? = null
  var worldcatId: Long? = null
  var storePrice = 0
  var storeLowestPrice: Int? = null
  var lowestPrice: Int? = null
  var discountRate: Double? = null
  var imageHeight = 0
  var imageWidth = 0
  var createdAt = 0L
  var updatedAt = 0L
  var lastFetchedAt = 0L
  var merchandisesCount = 0

  var category: Category? = null
  var author: Author? = null
  var publisher: Author? = null
  var pressRelation: Author? = null

  var lastMerchandiseId: Int? = null
  var lowestPriceMerchandise: Merchandise? = null

  var isLiked = false

  // utils

  fun width() = if (imageWidth == 0) 164 else imageWidth
  fun height() = if (imageHeight == 0) 235 else imageHeight

  fun isSeries() = parentId != null
  fun isNoSellerYet() = lastMerchandiseId == null
  fun isAvailable() = lowestPriceMerchandise != null
  fun isSoldOut() = !isNoSellerYet() && !isAvailable()
  fun titleText() = if (isSeries()) lowestPriceMerchandise?.seriesDescription else title
  fun publisherText() = {
    val author = author?.name ?: ""
    val publisher = publisher?.name ?: ""
    "$author" + (if (author.isNotEmpty() && publisher.isNotEmpty()) "/" else "") + "$publisher"
  }()
  fun publishedText() = {
    val dateFormatString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = if (publishedAt != null) dateFormatString.parse(publishedAt) else null
    if (date != null) {
      val calendar = Calendar.getInstance()
      calendar.time = date
      "${calendar.get(Calendar.YEAR)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.DATE)}"
    } else ""
  }()
}
