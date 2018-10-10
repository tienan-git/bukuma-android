package jp.com.labit.bukuma.analytic

/**
 * Created by zoonooz on 11/22/2016 AD.
 * event name for analytic. These will be sent to analytic services as key.
 */
class Event {
  companion object {
    val INSTALL = "install"
    val SCREEN_VIEW = "screen_view"
    val ACCOUNT_REGISTER = "register_account"
    val ACCOUNT_DELETE = "delete_account"
    val MERCHANDISE_CREATE = "create_merchandise"
    val MERCHANDISE_UPDATE = "update_merchandise"
    val MERCHANDISE_DELETE = "delete_merchandise"
    val MERCHANDISE_PURCHASE = "purchase_merchandise"
    val TRANSACTION_SHIPPED = "item_transaction_shipped"
    val TRANSACTION_ARRIVED = "item_transaction_buyer_arrived"
    val TRANSACTION_FINISHED = "item_transaction_buyer_finished"
    val BOOK_SEARCH_TITLE = "search_book_title"
    val ROOM_CREATE = "create_room"
    val MESSAGE_TEXT_SEND = "send_message_text"
    val MESSAGE_IMAGE_SEND = "send_message_image"
    val MESSAGE_BOOK_SEND = "send_message_book_mention"
  }
}
