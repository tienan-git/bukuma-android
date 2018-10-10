package jp.com.labit.bukuma.ui.fragment.home

import android.content.Intent
import android.os.Bundle
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.toJson
import jp.com.labit.bukuma.model.Transaction
import jp.com.labit.bukuma.ui.activity.ProfileActivity
import jp.com.labit.bukuma.ui.activity.TransactionActivity
import jp.com.labit.bukuma.ui.adapter.BaseAdapter
import jp.com.labit.bukuma.ui.adapter.TodoAdapter
import jp.com.labit.bukuma.ui.fragment.BaseListFragment
import kotlinx.android.synthetic.main.activity_chat.*
import rx.Single
import java.util.*

/**
 * Created by zoonooz on 10/24/2016 AD.
 * To do fragment (Transactions)
 */
class TodoFragment : BaseListFragment<Transaction>() {

  override val emptyTitleText: String? get() = getString(R.string.todo_empty_title)
  override val emptyDescriptionText: String? get() = getString(R.string.todo_empty_description)
  override val emptyImageResourceId: Int get() = R.drawable.img_ph_03
  override val fetchWhenResume: Boolean get() = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    fetchMoreOnScrollEnd = false
  }

  override fun adapter(): BaseAdapter<Transaction>? {
    if (service.currentUser?.id != null) {
      val adapter = TodoAdapter(service.currentUser!!.id)

      adapter.itemClick.subscribe {
        val intent = Intent(activity, TransactionActivity::class.java)
        intent.putExtra(TransactionActivity.EXTRA_TRANSACTION_ID, it.id)
        startActivity(intent)
      }

      adapter.userClick.subscribe {
        val isBuyer = it.user?.id ?: 0 == service.currentUser?.id
        val user = if (isBuyer) it.seller else it.user
        user?.let {
          val intent = Intent(activity, ProfileActivity::class.java)
          intent.putExtra(ProfileActivity.EXTRA_USER, it.toJson())
          startActivity(intent)
        }
      }

      return adapter
    }
    return null
  }

  override fun fetchStream(page: Int): Single<List<Transaction>>? {
    return service.api.getTransactionsAll().map { it.itemTransactions }
  }

  override fun fetchDisplay(page: Int, list: List<Transaction>) {
    val adapter = recyclerview.adapter as TodoAdapter
    val items = if (page == START_PAGE) {
      ArrayList()
    } else {
      adapter.items
    }
    items.addAll(list)
    val todoItems = ArrayList<Transaction>()
    val waitingItems = ArrayList<Transaction>()
    val currentUser = service.currentUser!!
    items.forEachIndexed { _, transaction ->
      val isBuyer = transaction.user?.id ?: 0 == currentUser.id
      when (transaction.status) {
        Transaction.STATUS_SELLER_SHIPPED -> {
          if (isBuyer) {
            todoItems.add(transaction)
          } else {
            waitingItems.add(transaction)
          }
        }
        Transaction.STATUS_SELLER_PREPARE,
        Transaction.STATUS_ITEM_ARRIVED -> {
          if (isBuyer) {
            waitingItems.add(transaction)
          } else {
            todoItems.add(transaction)
          }
        }
        Transaction.STATUS_INITIAL,
        Transaction.STATUS_FINISHED,
        Transaction.STATUS_PENDING_STAFF,
        Transaction.STATUS_CANCELLED-> {
          waitingItems.add(transaction)
        }
      }
    }
    adapter.items.clear()
    adapter.items.addAll(todoItems)
    adapter.items.addAll(waitingItems)
    adapter.todoCount = todoItems.size
    adapter.isNoTodo = todoItems.isEmpty()
    adapter.isNoWaiting = waitingItems.isEmpty()
    adapter.notifyDataSetChanged()
  }
}