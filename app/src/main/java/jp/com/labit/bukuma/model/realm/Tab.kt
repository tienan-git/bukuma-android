package jp.com.labit.bukuma.model.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by tani on 2017/05/08.
 */
open class Tab : RealmObject() {
  @PrimaryKey var id = 0
  var name = ""
  var url = ""
}