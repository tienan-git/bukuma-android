package jp.com.labit.bukuma.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.jakewharton.rxbinding.view.RxView
import com.jakewharton.rxbinding.widget.RxTextView
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.toJson
import jp.com.labit.bukuma.extension.toObject
import jp.com.labit.bukuma.fcm.NotificationCreator
import jp.com.labit.bukuma.model.Merchandise
import jp.com.labit.bukuma.model.realm.ChatMember
import jp.com.labit.bukuma.model.realm.ChatMerchandise
import jp.com.labit.bukuma.model.realm.ChatMessage
import jp.com.labit.bukuma.model.realm.ChatRoom
import jp.com.labit.bukuma.ui.adapter.ChatMessageAdapter
import jp.com.labit.bukuma.ui.custom.RecyclerViewScrollEndListener
import jp.com.labit.bukuma.util.infoDialog
import jp.com.labit.bukuma.util.loadingDialog
import kotlinx.android.synthetic.main.activity_chat.*
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 10/7/2016 AD.
 * Chat activity
 */
class ChatActivity : BaseImageChooserActivity() {

  companion object {
    val EXTRA_ROOM_ID = "extra_room_id"

    /**
     * keep track of current room in focus to prevent notification
     */
    var currentRoomId: Long? = null
  }

  private val REQUEST_MERCHANDISE = 444
  private val REFRESH_INTERVAL: Long = 4
  private val MAX_LINES = 3

  var messageHeight = 0

  var roomId = 0L
  lateinit var room: ChatRoom
  lateinit var realm: Realm
  lateinit var realmResult: RealmResults<ChatMessage>
  lateinit var timeSubscription: Subscription
  lateinit var adapter: ChatMessageAdapter

  // use for track last message from server and query for the next page
  private var lastMessageSync: Long = 0
  private var attachMerchandise: Merchandise? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_chat)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    roomId = intent.getLongExtra(EXTRA_ROOM_ID, 0)
    if (roomId == 0L) {
      Timber.e("no room extra")
      finish()
      return
    }

    val layoutManager = LinearLayoutManager(this)
    layoutManager.reverseLayout = true
    recyclerview.layoutManager = layoutManager
    recyclerview.setHasFixedSize(true)

    // realm open and listen to change
    realm = Realm.getDefaultInstance()
    val dbRoom = realm.where(ChatRoom::class.java)
        .equalTo("id", roomId)
        .findFirst()
    if (dbRoom == null) {
      finish()
      return
    }
    room = dbRoom

    // title & hide merchandise first
    getOpponentChatMember(room)?.let { supportActionBar?.title = it.nickname }
    merchandise_layout.visibility = View.GONE

    // messages
    realmResult = realm.where(ChatMessage::class.java)
        .equalTo("roomId", roomId)
        .findAllSorted("createdAt", Sort.DESCENDING)


    adapter = ChatMessageAdapter(service.currentUser!!.id, realmResult)
    recyclerview.adapter = adapter

    recyclerview.viewTreeObserver.addOnGlobalLayoutListener {
      val win = recyclerview.height - chat_layout.height
      if (messageHeight != 0) layoutManager.stackFromEnd = win > messageHeight
    }


    // load more history
    recyclerview.addOnScrollListener(object : RecyclerViewScrollEndListener(layoutManager) {
      override fun onLoadMore(current: Int) {
        Timber.i("chat next page after : $lastMessageSync")
        // load next page from the last message sync if not zero
        if (lastMessageSync != 0L) {
          service.chatRooms.getChatMessages(roomId, null, lastMessageSync)
              .subscribe({
                Timber.i("load next message page to : $lastMessageSync")
                // update last message sync
                it.lastOrNull()?.let { lastMessageSync = it.id }
              }, {
                Timber.e(it, "load next message failed")
              })
        }
      }
    })

    // actions -------------------------

    adapter.itemClick.subscribe { msg ->
      // error -> show option for delete, resend
      if (msg.isError) {
        val options = arrayOf<CharSequence>(
            getString(R.string.chat_send_error_option_delete),
            getString(R.string.chat_send_error_option_resend))

        AlertDialog.Builder(this)
            .setItems(options) { dialogInterface, i ->
              when (i) {
                0 -> deleteMessage(msg)
                1 -> resendMessage(msg)
              }
            }.setNegativeButton(getString(R.string.cancel), null)
            .create().show()
      } else {
        // transaction
        if (msg.itemTransaction != null) {
          val intent = Intent(this, TransactionActivity::class.java)
          intent.putExtra(TransactionActivity.EXTRA_TRANSACTION_ID, msg.itemTransaction!!.id)
          startActivity(intent)
        } else if (msg.merchandise?.bookId ?: 0 != 0) {
          // load book first
          val progress = loadingDialog(this)
          service.api.getBook(msg.merchandise!!.bookId)
              .subscribeOn(Schedulers.newThread())
              .observeOn(AndroidSchedulers.mainThread())
              .doOnEach { progress.dismiss() }
              .subscribe({
                Timber.i("chat success get book")
                val intent = Intent(this, BookActivity::class.java)
                intent.putExtra(BookActivity.EXTRA_BOOK, it.book?.toJson())
                startActivity(intent)
                this.finish()
              }, {
                Timber.e(it, "chat failed to get book")
                infoDialog(this, getString(R.string.error_tryagain))
              })
        } else if (msg.attachment != null) {
          val intent = Intent(this, ImageViewerActivity::class.java)
          intent.putExtra(ImageViewerActivity.EXTRA_URL, msg.attachment)
          startActivity(intent)
        }
      }
    }

    // merchandise pick
    RxView.clicks(add_button).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      val intent = Intent(this, MerchandiseChooserActivity::class.java)
      intent.putExtra(MerchandiseChooserActivity.EXTRA_USER_ID, room.friendId)
      startActivityForResult(intent, REQUEST_MERCHANDISE)
    }

    RxView.clicks(merchandise_close_button).subscribe {
      attachMerchandise = null
      merchandise_layout.visibility = View.GONE
    }

    // image pick
    RxView.clicks(photo_button).throttleFirst(1, TimeUnit.SECONDS).subscribe { chooseImage() }

    // text length to enable send button
    RxTextView.textChanges(message_edittext).subscribe {
      send_button.isEnabled = it.isNotEmpty()
    }

    RxTextView.textChangeEvents(message_edittext)
      .map { minOf(it.view().lineCount, MAX_LINES) }
      .doOnNext(message_edittext::setLines)
      .subscribe()

    // send text
    RxView.clicks(send_button).throttleFirst(1, TimeUnit.SECONDS).subscribe {
      val msg = ChatMessage()
      msg.messageType = ChatMessage.Companion.Type.TEXT.stringValue
      msg.text = message_edittext.text.toString()
      attachMerchandise?.let {
        msg.merchandise = ChatMerchandise().apply {
          id = it.id
          price = it.price
          soldAt = it.soldAt
          bookTitle = it.title() ?: ""
          it.book?.let {
            bookCover = it.coverImageUrl
            bookId = it.id
            bookWidth = it.imageWidth
            bookHeight = it.imageHeight
          }
        }
        msg.messageType = ChatMessage.Companion.Type.MERCHANDISE.stringValue
      }
      createMessage(room, msg)
      message_edittext.setText("")
      merchandise_layout.visibility = View.GONE
      attachMerchandise = null
    }
  }

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    // 試行錯誤した結果チャットメッセージの各々の高さがここでしか取れなかったのでここで取得しています
    for (i in realmResult.indices) {
      messageHeight += findViewById(R.id.content_layout).height
    }
  }

  override fun onDestroy() {
    realm.close()
    super.onDestroy()
  }

  override fun onResume() {
    super.onResume()
    currentRoomId = roomId
    // start update message
    startUpdate()

    // clear notification
    val notificationManager = NotificationManagerCompat.from(this)
    notificationManager.cancel(NotificationCreator.Type.MESSAGE.value, roomId.toInt())
  }

  override fun onPause() {
    super.onPause()
    currentRoomId = null
    // stop updating message list
    stopUpdate()
  }

  fun getOpponentChatMember(room: ChatRoom): ChatMember? {
    return room.members.firstOrNull { it.id != service.currentUser!!.id }
  }

  fun getSelfChatMember(room: ChatRoom): ChatMember {
    return room.members.first { it.id == service.currentUser!!.id }
  }

  // update room every time interval

  fun startUpdate() {
    timeSubscription = Observable.interval(0, REFRESH_INTERVAL, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
        // if first time or never get any image get message from zero, else use the recent one
        .map { if (lastMessageSync == 0L) 0 else realmResult.firstOrNull { it.isSent }?.id ?: 0 }
        .observeOn(Schedulers.newThread())
        .flatMap { service.chatRooms.getChatMessages(roomId, it).toObservable() }
        .subscribe({
          Timber.i("message interval update")
          // if this is first update or never get any message, set the last message id from server
          if (lastMessageSync == 0L) {
            lastMessageSync = it.lastOrNull()?.id ?: 0
          }
          // mark last message as read
          it.firstOrNull()?.let { msg ->
            service.api.readMessage(roomId, msg.id)
                .subscribeOn(Schedulers.newThread())
                .subscribe({
                  Timber.i("mark message read success : ${msg.id}")
                }, {
                  Timber.e(it, "mark message read failed : ${msg.id}")
                })
          }
        }, {
          Timber.e(it, "message interval update error")
        })
  }

  fun stopUpdate() {
    timeSubscription.unsubscribe()
  }

  // create message

  fun createMessage(room: ChatRoom, message: ChatMessage) {
    message.roomId = room.id
    message.user = getSelfChatMember(room)
    service.chatRooms.createChatMessage(message).subscribe({
      Timber.i("create message success")
    }, {
      Timber.e(it, "create message error")
    })

    when (message.type()) {
      ChatMessage.Companion.Type.TEXT -> tracker.trackMessageTextSend()
      ChatMessage.Companion.Type.IMAGE -> tracker.trackMessageImageSend()
      ChatMessage.Companion.Type.MERCHANDISE -> tracker.trackMessageBookMention()
      else -> {}
    }
  }

  fun resendMessage(message: ChatMessage) {
    service.chatRooms.createChatMessage(message).subscribe({
      Timber.i("resend message success")
    }, {
      Timber.e(it, "resend message error")
    })
  }

  fun deleteMessage(message: ChatMessage) {
    realm.beginTransaction()
    message.deleteFromRealm()
    realm.commitTransaction()
  }

  // attachment image

  override fun onGotImage(path: String) {
    val msg = ChatMessage()
    msg.messageType = ChatMessage.Companion.Type.IMAGE.stringValue
    msg.attachment = path
    createMessage(room, msg)
  }

  // activity result - choose merchandise

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == REQUEST_MERCHANDISE) {
      if (resultCode == Activity.RESULT_OK) {
        val merchandise = data!!
            .getStringExtra(MerchandiseChooserActivity.RESULT_MERCHANDISE)
            .toObject(Merchandise::class.java)!!

        attachMerchandise = merchandise
        merchandise_layout.visibility = View.VISIBLE
        merchandise_title_textview.text = merchandise.title()
        merchandise_desc_textview.text = if (merchandise.isSold()) {
          getString(R.string.book_detail_sold_out)
        } else getString(R.string.money_unit, merchandise.price)

        merchandise_imageview.setImageBitmap(null)
        val cover = merchandise.book?.coverImageUrl
        if (cover != null) {
          picasso.load(cover).into(merchandise_imageview)
        } else {
          picasso.load(R.drawable.img_book_placeholder).into(merchandise_imageview)
        }
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data)
    }
  }
}
