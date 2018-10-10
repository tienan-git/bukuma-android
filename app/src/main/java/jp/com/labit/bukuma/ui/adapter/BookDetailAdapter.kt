package jp.com.labit.bukuma.ui.adapter

import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.RxView
import com.jakewharton.rxbinding.view.clicks
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.Book
import jp.com.labit.bukuma.model.Merchandise
import jp.com.labit.bukuma.model.Tag
import jp.com.labit.bukuma.ui.viewholder.*
import rx.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 10/17/2016 AD.
 * Book detail adapter
 */
class BookDetailAdapter(var book: Book, var currentUserId: Int? = null) : BaseAdapter<Merchandise>() {

  val TYPE_BOOK = 1
  val TYPE_MERCHANDISE = 0
  val TYPE_MERCHANDISE_HEADER = 2
  val TYPE_ACTION_HEADER = 3
  val TYPE_ACTION_SELL = 4
  val TYPE_ACTION_REPORT = 5
  val TYPE_ACTION_MORE_ITEMS = 6
  val TYPE_TAG_HEADER = 7
  val TYPE_TAG = 8
  val TYPE_ACTION_MORE_TAGS = 9
  val TYPE_ACTION_RAKUTEN = 10

  val tagsHeaderPosition = 1

  var itemClick = PublishSubject.create<Merchandise>()
  var itemUserClick = PublishSubject.create<Merchandise>()
  var itemBuyClick= PublishSubject.create<Merchandise>()
  var itemQuickBuyClick = PublishSubject.create<Merchandise>()
  var itemSellClick = PublishSubject.create<Void>()
  var itemReportClick = PublishSubject.create<Void>()
  var itemRakutenClick = PublishSubject.create<Void>()
  var itemLikeClick = PublishSubject.create<Pair<Book, Int>>()
  var itemImageClick = PublishSubject.create<String>()
  var addTagsClick = PublishSubject.create<Void>()
  var voteTagClick = PublishSubject.create<Pair<Tag, Int>>()
  var tagClick = PublishSubject.create<Int>()
  var moreTagsClick = PublishSubject.create<Void>()

  var isLessItems = true
  var isLessTags = true

  var addTagsButtonHighLighted = false
    set(value) {
      field = value
      notifyItemChanged(tagsHeaderPosition)
    }

  var tags = listOf<Tag>()

  val displayItemsCount: Int
    get() = if (isLessItems) minOf(items.size, 5) else items.size

  val isDisplayMoreItems: Boolean
    get() = isLessItems && items.size > 5

  val displayTagsCount: Int
    get() = if (isLessTags) minOf(tags.count(), 5) else tags.count()

  val isDisplayMoreTags: Boolean
    get() = isLessTags && tags.count() > 5

  override fun onDataRefreshed() {
    isLessItems = true
    isLessTags = true
  }

  override fun getItemViewType(position: Int): Int {
    var index = position
    when (index) {
      0 -> return TYPE_BOOK
      1 -> return TYPE_TAG_HEADER
    }
    index -= 2

    val tagsCount = displayTagsCount
    if (index < tagsCount) return TYPE_TAG
    index -= tagsCount

    if (isDisplayMoreTags) {
      if (index == 0) return TYPE_ACTION_MORE_TAGS
      else index -= 1
    }

    if (items.isNotEmpty()) {
      if (index == 0) return TYPE_MERCHANDISE_HEADER
      else index -= 1
    }

    val itemsCount = displayItemsCount
    if (index < itemsCount) return TYPE_MERCHANDISE
    index -= itemsCount

    if (isDisplayMoreItems) {
      if (index == 0) return TYPE_ACTION_MORE_ITEMS
      else index -= 1
    }

    return when (index) {
      0 -> TYPE_ACTION_HEADER
      1 -> TYPE_ACTION_SELL
      2 -> if (book.rakutenLink.isNullOrEmpty()) TYPE_ACTION_REPORT else TYPE_ACTION_RAKUTEN
      3 -> TYPE_ACTION_REPORT
      else -> {
        Timber.e("invalid position: $index")
        TYPE_MERCHANDISE
      }
    }
  }

  override fun getItemCount(): Int {
    var count = 2 // book info & tag header
    count += displayTagsCount
    if (isDisplayMoreTags) count += 1

    if (items.isNotEmpty()) count += 1 // merchandise header
    count += displayItemsCount
    if (isDisplayMoreItems) count += 1

    count += 3 // others
    if (!book.rakutenLink.isNullOrEmpty()) count += 1
    return count
  }

  override fun getRealItemPosition(position: Int): Int {
    var count = 2 // book info & tag header
    count += displayTagsCount
    if (isDisplayMoreTags) count += 1
    if (items.isNotEmpty()) count += 1 // merchandise header

    return position - count
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      TYPE_BOOK -> BookDetailViewHolder(parent)
      TYPE_MERCHANDISE_HEADER, TYPE_ACTION_HEADER -> HeaderWhiteViewHolder(parent)
      TYPE_TAG_HEADER -> HeaderWithCircleButtonViewHolder(parent)
      TYPE_ACTION_SELL, TYPE_ACTION_REPORT, TYPE_ACTION_RAKUTEN -> ActionBookViewHolder(parent)
      TYPE_ACTION_MORE_ITEMS, TYPE_ACTION_MORE_TAGS -> ActionMoreLookViewHolder(parent)
      TYPE_TAG -> TagViewHolder(parent)
      else -> MerchandiseViewHolder(parent)
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val context = holder.itemView.context
    if (holder is BookDetailViewHolder) {
      holder.setObject(book, tags)
      book.lowestPriceMerchandise?.let { m ->
        RxView.clicks(holder.lowestBuyButton).throttleFirst(1, TimeUnit.SECONDS).subscribe {
          itemQuickBuyClick.onNext(m)
        }
      }

      RxView.clicks(holder.likeView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        itemLikeClick.onNext(Pair(book, position))
      }
      
      holder.tagLinkClick.throttleFirst(1, TimeUnit.SECONDS).share().subscribe {
        val url = Uri.parse(it)
        if (url.path == "tag") {
          url.getQueryParameter("id")?.let {
            tagClick.onNext(it.toInt())
          }
        }
      }
    } else if (holder is HeaderWhiteViewHolder) {
      holder.setVisibleTopMargin(true)
      when (getItemViewType(position)) {
        TYPE_ACTION_HEADER -> holder.setObject(context.getString(R.string.book_detail_title_action))
        TYPE_MERCHANDISE_HEADER ->
          holder.setObject(context.getString(R.string.book_detail_title_merchandise, items.size))
      }
    } else if (holder is HeaderWithCircleButtonViewHolder) {
      when (getItemViewType(position)) {
        TYPE_TAG_HEADER -> {
          holder.setObject(context.getString(R.string.book_detail_title_tags))
          holder.circleButtonTitle = context.getString(R.string.book_detail_action_add_tag)
          holder.setCircleButtonHighLighted(addTagsButtonHighLighted)
          RxView.clicks(holder.circleButtonView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
            addTagsClick.onNext(null)
          }
        }
      }
    } else if (holder is ActionBookViewHolder) {
      when (getItemViewType(position)) {
        TYPE_ACTION_SELL -> {
          holder.setObject(context.getString(R.string.book_detail_action_sell), R.drawable.ic_product_sell)
          RxView.clicks(holder.itemView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
            itemSellClick.onNext(null)
          }
        }
        TYPE_ACTION_REPORT -> {
          holder.setObject(context.getString(R.string.book_detail_action_report), R.drawable.ic_product_report)
          RxView.clicks(holder.itemView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
            itemReportClick.onNext(null)
          }
        }
        TYPE_ACTION_RAKUTEN -> {
          holder.setObject(context.getString(R.string.book_detail_action_rakuten), R.drawable.ic_product_amazon)
          RxView.clicks(holder.itemView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
            itemRakutenClick.onNext(null)
          }
        }
      }
    } else if (holder is MerchandiseViewHolder) {
      val merchandise = getItem(position)
      val isSelf = currentUserId == merchandise.user?.id && currentUserId != null
      holder.setObject(merchandise, isSelf)

      RxView.clicks(holder.merchandiseLayout).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        itemClick.onNext(merchandise)
      }
      RxView.clicks(holder.userLayout).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        itemUserClick.onNext(merchandise)
      }
      RxView.clicks(holder.buyButton).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        itemBuyClick.onNext(merchandise)
      }

      // series images
      RxView.clicks(holder.series1ImageView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        itemImageClick.onNext(merchandise.imageUrl)
      }
      RxView.clicks(holder.series2ImageView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        itemImageClick.onNext(merchandise.image_2Url)
      }
      RxView.clicks(holder.series3ImageView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
        itemImageClick.onNext(merchandise.image_3Url)
      }
    } else if (holder is TagViewHolder) {
      val tag = tags[position - 2]
      holder.setObject(tag)
      holder.countView.clicks().throttleFirst(1, TimeUnit.SECONDS).subscribe {
        voteTagClick.onNext(Pair(tag, position))
      }
      holder.itemView.clicks().throttleFirst(1, TimeUnit.SECONDS).subscribe {
        tagClick.onNext(tag.id)
      }
    } else if (holder is ActionMoreLookViewHolder) {
      when (getItemViewType(position)) {
        TYPE_ACTION_MORE_ITEMS -> {
          holder.setObject(context.getString(R.string.book_detail_action_more_look))
          RxView.clicks(holder.itemView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
            isLessItems = false
            this.notifyDataSetChanged()
          }
        }
        TYPE_ACTION_MORE_TAGS -> {
          holder.setObject(context.getString(R.string.book_detail_action_more_look))
          RxView.clicks(holder.itemView).throttleFirst(1, TimeUnit.SECONDS).subscribe {
            moreTagsClick.onNext(null)
          }
        }
      }
    }
  }
}
