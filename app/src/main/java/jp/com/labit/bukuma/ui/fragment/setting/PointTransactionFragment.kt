package jp.com.labit.bukuma.ui.fragment.setting

import jp.com.labit.bukuma.model.PointTransaction
import jp.com.labit.bukuma.ui.adapter.BaseAdapter
import jp.com.labit.bukuma.ui.adapter.PointTransactionAdapter
import jp.com.labit.bukuma.ui.fragment.BaseListFragment
import kotlinx.android.synthetic.main.fragment_list.*
import rx.Single
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by zoonooz on 11/14/2016 AD.
 * Point transaction history
 */
class PointTransactionFragment : BaseListFragment<PointTransaction>() {

  var sale: Int? = 0
  var bonus: Int? = 0
  var expiringPoint: Int? = 0
  var expiringDatetime: Long = 0L
  var originalList: MutableList<PointTransaction> = mutableListOf()
  private val dateFormatString = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

  override fun adapter(): BaseAdapter<PointTransaction>? {
    return PointTransactionAdapter()
  }

  override fun fetchStream(page: Int): Single<List<PointTransaction>>? {
    return service.api.getPointTransactions(page).map {
      sale = it.userPoint
      bonus = it.userBonusPoint
      expiringPoint = it.nearExpireBonusPoint
      expiringDatetime = it.nearExpireBonusPointDatetime
      it.pointTransactions
    }
  }

  override fun fetchDisplay(page: Int, list: List<PointTransaction>) {
    val adapter = recyclerview.adapter as PointTransactionAdapter
    if (page == START_PAGE) {
      originalList.clear()
    }

    adapter.sale = sale
    if (bonus == 0) adapter.isNotAppeared = true
    adapter.bonus = bonus
    adapter.expiringPoint = expiringPoint
    var expStr = dateFormatString.format(Date(expiringDatetime * 1000))
    if (expiringDatetime == 0L) expStr = ""
    adapter.expiringDayTime = expStr

    originalList.addAll(list)
    val adjusted = adjustItems(originalList)
    adapter.items.clear()
    adapter.items.addAll(adjusted)
    adapter.notifyDataSetChanged()
  }

  /**
   * Adjust point transaction to show better info for user
   */
  fun adjustItems(old: List<PointTransaction>): List<PointTransaction> {
    val newItems = ArrayList<PointTransaction>()
    var processing: PointTransaction? = null
    var processing2: PointTransaction? = null
    old.forEachIndexed { i, transaction ->
      val type = transaction.type()
      val merchandise = transaction.merchandise?.id ?: 0

      if (type == PointTransaction.TYPE_BUY_MERCHANDISE && processing2 == null) {
        if (processing == null) {
          processing = transaction
          return@forEachIndexed
        }
        if (processing2 == null && merchandise == processing!!.merchandise?.id ?: 0){
          processing2 = transaction
          return@forEachIndexed
        }
      }

      if (type == PointTransaction.TYPE_BUY_POINT) {
        processing?.let {
          val originalPointUse = it.pointChanged + transaction.pointChanged
          if (originalPointUse == 0) {
            it.description = "Bought from card"
            newItems.add(it)
            processing2?.let { newItems.add(it) }
          } else {
            it.pointChanged = originalPointUse
            newItems.add(it)
            processing2?.let { newItems.add(it) }
            transaction.pointChanged = -transaction.pointChanged
            transaction.description = "Bought from card"
            transaction.book = it.book
            newItems.add(transaction)
          }
        }

        processing = null
        processing2 = null
        return@forEachIndexed
      }

      if (type == PointTransaction.TYPE_BUY_FROM_CARD) {
        transaction.creditPoint = -transaction.creditPoint
        newItems.add(transaction)
        return@forEachIndexed
      }

      processing?.let { newItems.add(it) }
      processing2?.let { newItems.add(it) }
      processing = null
      processing2 = null
      newItems.add(transaction)
    }

    return newItems
  }
}