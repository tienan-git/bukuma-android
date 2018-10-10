package jp.com.labit.bukuma.ui.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.jakewharton.rxbinding.view.RxView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.*
import jp.com.labit.bukuma.model.Book
import jp.com.labit.bukuma.model.Merchandise
import jp.com.labit.bukuma.ui.activity.*
import jp.com.labit.bukuma.ui.activity.drawer.ReportActivity
import jp.com.labit.bukuma.ui.adapter.BaseAdapter
import jp.com.labit.bukuma.ui.adapter.BookDetailAdapter
import jp.com.labit.bukuma.ui.viewholder.BookDetailViewHolder
import jp.com.labit.bukuma.ui.viewholder.TagViewHolder
import jp.com.labit.bukuma.util.RxAlertDialog
import jp.com.labit.bukuma.util.infoDialog
import kotlinx.android.synthetic.main.fragment_list.*
import rx.Observable
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber

/**
 * Created by zoonooz on 10/17/2016 AD.
 * Book detail fragment
 */
class BookDetailFragment : BaseListFragment<Merchandise>() {

  interface Listener {
    fun onClickAddTagsButton()
  }

  private val REQUEST_MERCHANDISE = 980
  lateinit var book: Book

  private var listener: Listener? = null

  private val bookDetailAdapter: BookDetailAdapter?
    get() = adapter as? BookDetailAdapter

  companion object {
    val ARG_BOOK = "arg_book"
    fun newInstance(book: Book): BookDetailFragment {
      val fragment = BookDetailFragment()
      val args = Bundle()
      args.putString(ARG_BOOK, book.toJson())
      fragment.arguments = args
      return fragment
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    book = arguments.getString(ARG_BOOK)?.toObject(Book::class.java)
        ?: throw IllegalArgumentException("need book json object")
  }

  override fun onAttach(context: Context?) {
    listener = context as? Listener
    super.onAttach(context)
  }

  override fun onDetach() {
    super.onDetach()
    listener = null
  }

  override fun fetch(page: Int) {
    super.fetch(page)

    if (page == START_PAGE) {
      service.api.getTags(book.id, 6).subscribeOn(Schedulers.newThread())
          .observeOn(AndroidSchedulers.mainThread())
          .takeUntil(RxView.detaches(recyclerview))
          .subscribe({ response ->
            bookDetailAdapter?.let {
              it.isLessTags = true
              it.tags = response.tags
              it.notifyDataSetChanged()
            }
          }, {})
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == REQUEST_MERCHANDISE) {
      if (resultCode == Activity.RESULT_OK) fetch()
    } else {
      super.onActivityResult(requestCode, resultCode, data)
    }
  }

  override fun adapter(): BaseAdapter<Merchandise>? {
    val adapter = BookDetailAdapter(book, service.currentUser?.id)

    adapter.itemUserClick.subscribe {
      if (it.user == null) {
        infoDialog(activity, getString(R.string.merchandise_error_user_deleted))
      } else {
        val intent = Intent(activity, ProfileActivity::class.java)
        intent.putExtra(ProfileActivity.EXTRA_USER, it.user?.toJson())
        startActivity(intent)
      }
    }

    adapter.itemQuickBuyClick.subscribe { purchase(it) }
    Observable.merge(adapter.itemBuyClick, adapter.itemClick)
            .subscribe { purchase(it, allowEdit = true) }

    adapter.itemSellClick.filterLoggedIn(baseActivity).subscribe {
      applyIfVerified(baseActivity) {
        val intent = Intent(activity, PublishActivity::class.java)
        intent.putExtra(PublishActivity.EXTRA_CREATE_BOOK, book.toJson())
        startActivityForResult(intent, REQUEST_MERCHANDISE)
      }
    }

    adapter.itemReportClick
        .filterLoggedIn(baseActivity)
        .flatMap {
          RxAlertDialog.alert2(activity,
              null,
              getString(R.string.book_report_confirm_message),
              getString(R.string.report),
              getString(R.string.cancel))
        }
        .filter { it }
        .observeOn(Schedulers.newThread())
        .flatMap { service.api.createReport(book.id, "Book").toObservable().toResponse() }
        .observeOn(AndroidSchedulers.mainThread())
        .filter { it is Response.Success }
        .flatMap {
          RxAlertDialog.alert2(activity,
              getString(R.string.book_report_success_title),
              getString(R.string.book_report_success_message),
              getString(R.string.book_report_success_report),
              getString(R.string.cancel))
        }
        .filter { it }
        .subscribe {
          val intent = Intent(activity, ReportActivity::class.java)
          intent.putExtra(ReportActivity.EXTRA_BOOK_ID, book.id)
          intent.putExtra(ReportActivity.EXTRA_BOOK_TITLE, book.titleText())
          startActivity(intent)
        }

    adapter.itemRakutenClick.subscribe {
      val intent = Intent(activity, WebActivity::class.java)
//      intent.putExtra(WebActivity.EXTRA_TITLE, getString(R.string.navigation_howto))
      intent.putExtra(WebActivity.EXTRA_URL, book.rakutenLink)
      startActivity(intent)
    }

    adapter.itemLikeClick.filterLoggedIn(baseActivity).subscribe {
      val book = it.first
      val position = it.second
      val vh = recyclerview.findViewHolderForLayoutPosition(position) as? BookDetailViewHolder
      book.isLiked = !book.isLiked
      book.cachedVotesTotal += if (book.isLiked) 1 else -1
      vh?.likeView?.setLike(book.isLiked, true)
      vh?.likeTextView?.text = "${book.cachedVotesTotal}"

      val observable = if (book.isLiked) {
        service.api.likeBook(book.id)
      } else {
        service.api.unlikeBook(book.id)
      }

      // call api
      observable
          .subscribeOn(Schedulers.newThread())
          .observeOn(AndroidSchedulers.mainThread())
          .map { recyclerview.findViewHolderForLayoutPosition(position) as? BookDetailViewHolder }
          .doOnError {
            book.isLiked = !book.isLiked
            book.cachedVotesTotal += if (book.isLiked) 1 else -1
            val h = recyclerview.findViewHolderForLayoutPosition(position) as? BookDetailViewHolder
            h?.likeView?.setLike(book.isLiked, false)
            h?.likeTextView?.text = "${book.cachedVotesTotal}"
          }
          .subscribe({
            Timber.i("like/unlike book success")
          }, {
            Timber.e(it, "like/unlike book failed")
          })
    }

    adapter.itemImageClick.subscribe {
      val intent = Intent(activity, ImageViewerActivity::class.java)
      intent.putExtra(ImageViewerActivity.EXTRA_URL, it)
      startActivity(intent)
    }

    adapter.addTagsClick.subscribe {
      applyIfLoggedIn(baseActivity) {
        if (adapter.isLessTags) {
          fetchTags()
        }
        listener?.onClickAddTagsButton()
        (recyclerview.layoutManager as? LinearLayoutManager)?.let {
          it.scrollToPositionWithOffset(adapter.tagsHeaderPosition, 0)
        }
        adapter.addTagsButtonHighLighted = true
      }
    }

    adapter.tagClick.subscribe {
      val intent = Intent(activity, TimelineActivity::class.java)
      intent.putExtra(TimelineActivity.EXTRA_URL, "v1/books/tags/${it}")
      startActivity(intent)
    }

    adapter.voteTagClick.subscribe {
      applyIfLoggedIn(baseActivity) {
        val tag = it.first
        val position = it.second
        val toggleVoted = {
          tag.isVoted = !tag.isVoted
          tag.votesCount += if (tag.isVoted) 1 else -1
          (recyclerview.findViewHolderForLayoutPosition(position) as? TagViewHolder)?.let {
            it.setObject(tag)
          }
        }

        toggleVoted()
        val request = if (tag.isVoted) {
          service.api.voteTag(book.id, tag.id)
        } else {
          service.api.unvoteTag(book.id, tag.id)
        }

        request
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { toggleVoted() }
            .subscribe({}, { Timber.e(it, "vote/unvote tag failed") })
      }
    }

    adapter.moreTagsClick.subscribe {
      fetchTags()
    }

    return adapter
  }

  override fun fetchStream(page: Int): Single<List<Merchandise>>? {
    return service.api.getMerchandises(book.id, page).map { it.merchandises.filter { it.user != null } }
  }

  // event

  fun onCreatedTag() {
    bookDetailAdapter?.let {
      fetchTags()
    }
  }

  fun onHideKeyboard() {
    bookDetailAdapter?.let {
      it.addTagsButtonHighLighted = false
    }
  }

  // util

  fun purchase(merchandise: Merchandise, allowEdit: Boolean = false) {
    applyIfVerified(baseActivity) {
      if (merchandise.user == null) {
        infoDialog(activity, getString(R.string.merchandise_error_user_deleted))
      } else if (merchandise.isSold()) {
        infoDialog(activity, getString(R.string.merchandise_error_sold))
      } else if (merchandise.user!!.id == service.currentUser!!.id) {
        // mine
        if (!allowEdit) {
          infoDialog(activity, getString(R.string.merchandise_error_self))
        } else {
          val intent = Intent(activity, PublishActivity::class.java)
          intent.putExtra(PublishActivity.EXTRA_EDIT_MERCHANDISE, merchandise.toJson())
          startActivityForResult(intent, REQUEST_MERCHANDISE)
        }
      } else {
        // redirect
        val intent = Intent(activity, PurchaseActivity::class.java)
        intent.putExtra(PurchaseActivity.EXTRA_MERCHANDISE_ID, merchandise.id)
        startActivity(intent)
      }
    }
  }

  private fun fetchTags() {
    service.api.getTagsAll(book.id).subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ response ->
          bookDetailAdapter?.let {
            it.isLessTags = false
            it.tags = response.tags
            it.notifyDataSetChanged()
          }
        }, {})
  }
}
