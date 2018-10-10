package jp.com.labit.bukuma.api.response

import jp.com.labit.bukuma.model.realm.Category

/**
 * Created by zoonooz on 9/9/2016 AD.
 * Response models for category
 */

// get categories list
class GetCategoriesResponse : BaseResponse() {
  var categories = emptyList<CategoryResponse>()

  class CategoryResponse {
    var id = 0
    var name = ""
    var categoryId: Int? = null
    var categoriesCount = 0
    var updatedAt: Long? = 0
    var categories = emptyArray<SubCategory>()
  }

  class SubCategory {
    var category: CategoryResponse? = null
  }

  fun convert(): List<Category> {
    return categories.map { convertToModel(it) }
  }

  private fun convertToModel(it: CategoryResponse): Category {
    val cat = Category()
    cat.id = it.id
    cat.name = it.name
    cat.categoryId = it.categoryId
    cat.categoriesCount = it.categoriesCount
    cat.updatedAt = it.updatedAt
    cat.categories.addAll(it.categories.map { it.category?.let { convertToModel(it) }})
    return cat
  }
}