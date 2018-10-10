package jp.com.labit.bukuma.model.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by zoonooz on 9/9/2016 AD.
 * Book category model
 */
open class Category : RealmObject() {
  @PrimaryKey var id = 0
  var name = ""
  var categoryId: Int? = null
  var categoriesCount = 0
  var updatedAt: Long? = 0
  var categories: RealmList<Category> = RealmList()

  companion object {
    // extra category
    val ID_BULK = -1
    val ID_PUBLISH6M = -2
    val ID_UNDER300 = -3
    val ID_ALL = -4

    // sub category
    val ID_HUMANITY = 294
    val ID_NOVEL = 329
    val ID_SOCIETY = 281
    val ID_NONFICTION = 279
    val ID_HISTORY = 270
    val ID_BUSINESS = 15
    val ID_INVESTMENT = 30
    val ID_IT = 2
    val ID_BEAUTY = 134
    val ID_HOBBY = 205
    val ID_MAGAZINE = 239
    val ID_COMIC = 213
    val ID_PICTURE = 198
    val ID_LANGUAGE = 161
    val ID_CAPABILITY = 117
    val ID_EDUCATION = 188
    val ID_SCIENCE = 41
    val ID_MEDICINE = 242
    val ID_SPORTS = 108
    val ID_JOURNEY = 155
    val ID_BUILDING = 57
    val ID_SCORE = 241
    val ID_ENTERTAINMENT = 229
    val ID_LIGHTNOVEL = 221
    val ID_FOREIGN = 341
    val ID_TALENT = 227
  }
}
