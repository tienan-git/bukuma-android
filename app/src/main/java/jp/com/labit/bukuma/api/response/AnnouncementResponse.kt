package jp.com.labit.bukuma.api.response

import jp.com.labit.bukuma.model.Announcement

/**
 * Created by zoonooz on 9/15/2016 AD.
 * response for announcement
 */

class GetAnnouncementResponse : BaseResponse() {
  var announcements = emptyList<Announcement>()
}

class GetAnnouncementUnreadResponse : BaseResponse() {
  var unreadCount = 0
}