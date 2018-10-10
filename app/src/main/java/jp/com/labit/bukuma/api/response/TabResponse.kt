package jp.com.labit.bukuma.api.response

import jp.com.labit.bukuma.model.realm.Tab

/**
 * Created by tani on 2017/05/08.
 */

class GetTabsResponse : BaseResponse() {
  var tabs = emptyList<TabResponse>()

  fun convert(): List<Tab> {
    return tabs.map { it.toModel() }
  }
}

class TabResponse {
  var id = 0
  var name = ""
  var url = ""

  fun toModel(): Tab {
    val tab = Tab()
    tab.id = id
    tab.name = name
    tab.url = url
    return tab
  }
}