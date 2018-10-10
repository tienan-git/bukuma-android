package jp.com.labit.bukuma.fcm

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import com.google.gson.JsonObject
import jp.com.labit.bukuma.BukumaApplication
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.getAsPrimitiveOrNull
import jp.com.labit.bukuma.extension.toJson
import jp.com.labit.bukuma.ui.activity.BookActivity
import jp.com.labit.bukuma.ui.activity.ChatActivity
import jp.com.labit.bukuma.ui.activity.MainActivity
import jp.com.labit.bukuma.ui.activity.TransactionActivity
import jp.com.labit.bukuma.ui.activity.drawer.NewsActivity

/**
 * Created by zoonooz on 11/4/2016 AD.
 * Notification creator
 */
class NotificationCreator {

  /**
   * Notification type
   * The type is need when creating the notification. It tells the creator how to read the
   * json data
   */
  enum class Type(val value: String) {
    /** Chat message notification type */
    MESSAGE("message"),

    /** New news available */
    NEWS("news"),

    /** Someone bought merchandise */
    MERCHANDISE_BOUGHT("merchandise_bought"),

    /** Someone like user's book */
    BOOK_LIKE("book_like"),

    /** Book price updated */
    BOOK_SELLING_PRICE("book_selling_price"),

    /** */
    BOOK_LIKE_SELLING_PRICE("book_liked_selling_price"),

    /** Transaction data or status updated */
    TRANSACTION_UPDATE("item_transaction_update"),

    /** Home */
    HOME("home"),

    /** Notification from firebase */
    FIREBASE("firebase");

    companion object {
      fun from(value: String): Type {
        return values().first { it.value == value }
      }
    }
  }

  private companion object {
    var notificationId = 0
  }

  /**
   * Show notification
   *
   * @param context context
   * @param type the type of notification
   * @param data the [JsonObject] data to build the notification
   */
  fun showNotification(context: Context, type: Type, data: JsonObject) {
    val notiId = notificationId(type, data)
    val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    val title = data.getAsPrimitiveOrNull("title")?.asString ?: context.getString(context.applicationInfo.labelRes)
    val message = data.getAsPrimitiveOrNull("message")?.asString

    val notificationBuilder = NotificationCompat.Builder(context)
        .setSmallIcon(R.drawable.ic_push)
        .setContentTitle(title)
        .setContentText(message)
        .setSound(defaultSoundUri)
        .setAutoCancel(true)
        .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
        .setStyle(NotificationCompat.BigTextStyle()
            .bigText(message)
            .setBigContentTitle(title))

    // add intent
    val intent = createIntent(context, type, data)
    if (intent != null) {
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      val pendingIntent = PendingIntent.getActivity(
          context,
          notiId /* Request code */,
          intent,
          PendingIntent.FLAG_ONE_SHOT)
      notificationBuilder.setContentIntent(pendingIntent)
    }

    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.notify(type.value, notiId, notificationBuilder.build())
  }

  private fun createIntent(context: Context, type: Type, data: JsonObject): Intent? {
    val service = (context.applicationContext as BukumaApplication).service
    var intent: Intent? = null
    when (type) {
    // preload room before show notification
      Type.MESSAGE -> {
        data["room_id"]?.asLong?.let {
          service.chatRooms.getChatRoom(it)
              .doOnSuccess { Thread.sleep(3000) } // wait 3 sec for server db problem
              .subscribe({}, {})

          intent = Intent(context, ChatActivity::class.java)
          intent?.putExtra(ChatActivity.EXTRA_ROOM_ID, it)
        }
      }
    // load book before show notification
      Type.BOOK_LIKE, Type.BOOK_LIKE_SELLING_PRICE, Type.BOOK_SELLING_PRICE -> {
        data["book"]?.asInt?.let {
          service.api.getBook(it).subscribe({
            it.book?.let {
              intent = Intent(context, BookActivity::class.java)
              intent?.putExtra(BookActivity.EXTRA_BOOK, it.toJson())
            }
          }, {})
        }
      }
      Type.TRANSACTION_UPDATE -> {
        data["item_transaction"]?.asInt?.let {
          intent = Intent(context, TransactionActivity::class.java)
          intent?.putExtra(TransactionActivity.EXTRA_TRANSACTION_ID, it)
        }
      }
      Type.NEWS -> {
        intent = Intent(context, NewsActivity::class.java)
      }
      Type.HOME -> {
        intent = Intent(context, MainActivity::class.java)
      }
      else -> {
      }
    }
    return intent
  }

  private fun notificationId(type: Type, data: JsonObject): Int {
    if (type == Type.MESSAGE) {
      return data["room_id"]?.asInt ?: type.ordinal
    } else if (type == Type.BOOK_LIKE || type == Type.BOOK_SELLING_PRICE || type == Type.BOOK_LIKE_SELLING_PRICE) {
      return data["book"]?.asInt?.let { it * 10 + type.ordinal} ?: type.ordinal
    }
    return System.currentTimeMillis().toInt() // できるだけUniqueなIntを返したい
  }
}
