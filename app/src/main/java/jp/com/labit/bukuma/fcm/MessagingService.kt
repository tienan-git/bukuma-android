package jp.com.labit.bukuma.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.JsonObject
import jp.com.labit.bukuma.BukumaApplication
import jp.com.labit.bukuma.extension.getAsPrimitiveOrNull
import jp.com.labit.bukuma.extension.toObject
import jp.com.labit.bukuma.ui.activity.ChatActivity
import timber.log.Timber

/**
 * Created by zoonooz on 11/2/2016 AD.
 * FCM Messaging service
 */
class MessagingService : FirebaseMessagingService() {

  override fun onMessageReceived(message: RemoteMessage) {
    Timber.i("From: ${message.from}")
    Timber.i("To: ${message.to}")
    Timber.i("Type: ${message.messageType}")
    Timber.i("Message: ${message.data}")

    val creator = NotificationCreator()
    val data = message.data["default"]?.toObject(JsonObject::class.java) ?: JsonObject()
    val typeStr = data.getAsPrimitiveOrNull("type")?.asString ?: NotificationCreator.Type.FIREBASE.value
    val type = NotificationCreator.Type.from(typeStr)

    // check where message come from
    if (type != NotificationCreator.Type.FIREBASE) {
      // show message only if room is not in focus
      if (type == NotificationCreator.Type.MESSAGE) {
        val roomId = data["room_id"]?.asLong
        if (roomId == ChatActivity.currentRoomId) return
      } else if (type == NotificationCreator.Type.MERCHANDISE_BOUGHT) {
        // dont show for now as it comes with transaction update
        return
      }
      creator.showNotification(this, type, data)
    } else {
      // firebase
      data.addProperty("title", message.notification.title)
      data.addProperty("message", message.notification.body)

      creator.showNotification(this, NotificationCreator.Type.FIREBASE, data)
    }

    // event
    val badgeCount = data.getAsPrimitiveOrNull("badge")?.asInt
    val info = NotificationInfo(type, badgeCount)
    (application as BukumaApplication).postNotification(info)
  }
}
