package jp.com.labit.bukuma.model

/**
 * Created by zoonooz on 10/3/2016 AD.
 * User address
 */
class Address {
  var id = 0
  var name = ""
  var address_1 = ""
  var address_2: String? = null
  var city = ""
  var prefecture = ""
  var postalCode = ""
  var country = ""
  var default = false
  var personName: String? = null
  var personNameKana: String? = null
  var telephone: String? = null
  var updatedAt: Long = 0

  /**
   * Address display text
   * @return formatted address text
   */
  fun addressText(): String {
    var address = "〒"
    address += notNull(postalCode)
    address += "\n"
    address += notNull(prefecture)
    address += " "
    address += notNull(city)
    address += " "
    address += notNull(address_1)
    address_2?.let { address += " ${notNull(it)}" }
    telephone?.let {
      val tel = notNull(it)
      if (tel.isNotBlank()) address += "\n$tel"
    }
    personName?.let {
      val name = notNull(it)
      if (name.isNotBlank()) address += "\n$name 様"
    }
    return address
  }

  fun addressOnlyText(): String {
    val builder = StringBuilder("〒")
    val postalCode = notNull(postalCode)
    if(postalCode.length > 3) {
      builder.append(postalCode.substring(0..2))
          .append("-")
          .appendln(postalCode.substring(3))
    } else {
      builder.appendln(postalCode)
    }

    builder.append(notNull(prefecture))
        .append(" ").append(notNull(city))
        .append(" ").append(notNull(address_1))

    address_2?.let { builder.appendln().append(notNull(it)) }

    telephone?.let {
      val tel = notNull(it)
      if (tel.isNotBlank()) builder.appendln().append(tel)
    }

    return builder.toString()
  }

  fun nameText(): String {
    return "${notNull(personName ?: "")} 様"
  }

  // check null (bug send <null> text to server from ios)
  private fun notNull(str: String): String {
    return if (str != "<null>") str else ""
  }
}
