package jp.com.labit.bukuma.database

import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration

/**
 * Created by zoonooz on 9/12/2016 AD.
 * Migration class for realm db. Please refer to [jp.com.labit.bukuma.BukumaApplication.setupRealm]
 * for update database version
 */
class Migration : RealmMigration {

  override fun migrate(realm: DynamicRealm?, oldVersion: Long, newVersion: Long) {
    if(realm == null) return

    val schema = realm.schema
    var version = oldVersion

    if(version == 0L) {
      schema.create("Tab")
        .addField("id", Int::class.java, FieldAttribute.PRIMARY_KEY)
        .addField("name", String::class.java)
        .addField("url", String::class.java)
      schema.get("User")
        .addField("isOfficial", Boolean::class.java)
      version++
    }

  }
}