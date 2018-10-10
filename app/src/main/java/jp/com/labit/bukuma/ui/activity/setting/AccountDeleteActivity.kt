package jp.com.labit.bukuma.ui.activity.setting

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import com.jakewharton.rxbinding.view.RxView
import com.jakewharton.rxbinding.widget.RxTextView
import jp.com.labit.bukuma.BukumaError
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.model.DeleteReason
import jp.com.labit.bukuma.ui.activity.BaseActivity
import jp.com.labit.bukuma.ui.dialog.DeleteAlertDialog
import jp.com.labit.bukuma.util.RxAlertDialog
import jp.com.labit.bukuma.util.infoDialog
import kotlinx.android.synthetic.main.activity_account_delete.*
import rx.Single
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 2017/01/12.
 * Account deletion activity
 */
class AccountDeleteActivity : BaseActivity(), DeleteAlertDialog.DeleteAlertDialogCallBack {

  var isLargeItemClicked = false
  var isSmallItemClicked = false
  val majorItems = ArrayList<RadioButton>()
  val minorItems = ArrayList<RadioButton>()
  val minorGroup = ArrayList<RadioGroup>()
  var selectDeleteItem = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_account_delete)

    // 退会理由の大項目
    majorItems.add(reason1_radio)
    majorItems.add(reason2_radio)
    majorItems.add(reason3_radio)
    majorItems.add(reason4_radio)
    majorItems.add(reason5_radio)
    majorItems.add(reason6_radio)
    majorItems.add(reason7_radio)
    majorItems.add(reason8_radio)
    majorItems.add(reason9_radio)

    // 退会理由の小項目
    minorItems.add(minor1_radio)
    minorItems.add(minor2_radio)
    minorItems.add(minor3_radio)
    minorItems.add(minor4_radio)
    minorItems.add(minor5_radio)
    minorItems.add(minor6_radio)
    minorItems.add(minor7_radio)
    minorItems.add(minor8_radio)
    minorItems.add(minor9_radio)
    minorItems.add(minor10_radio)
    minorItems.add(minor11_radio)
    minorItems.add(minor12_radio)
    minorItems.add(minor13_radio)
    minorItems.add(minor14_radio)
    minorItems.add(minor15_radio)
    minorItems.add(minor16_radio)
    minorItems.add(minor17_radio)
    minorItems.add(minor18_radio)
    minorItems.add(minor19_radio)
    minorItems.add(minor20_radio)
    minorItems.add(minor21_radio)
    minorItems.add(minor22_radio)
    minorItems.add(minor23_radio)
    minorItems.add(minor24_radio)
    minorItems.add(minor25_radio)
    minorItems.add(minor26_radio)
    minorItems.add(minor27_radio)
    minorItems.add(minor28_radio)
    minorItems.add(minor29_radio)
    minorItems.add(minor30_radio)
    minorItems.add(minor31_radio)
    minorItems.add(minor32_radio)
    minorItems.add(minor33_radio)
    minorItems.add(minor34_radio)
    minorItems.add(minor35_radio)
    minorItems.add(minor36_radio)
    minorItems.add(minor37_radio)
    minorItems.add(minor38_radio)

    // 退会理由の小項目のグループ
    minorGroup.add(reason1_radio_group)
    minorGroup.add(reason3_radio_group)
    minorGroup.add(reason4_radio_group)
    minorGroup.add(reason5_radio_group)
    minorGroup.add(reason6_radio_group)
    minorGroup.add(reason7_radio_group)
    minorGroup.add(reason8_radio_group)
    minorGroup.add(reason9_radio_group)

    supportActionBar?.title = getString(R.string.delete_user_confirm_ok)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    hideRadioGroup()

    val group: RadioGroup = findViewById(R.id.reason_radio_group) as RadioGroup
    group.setOnCheckedChangeListener { _, checkedId ->
      if (!isLargeItemClicked) {
        selectedRadioGroup(checkedId)
      } else {
        hideRadioGroup()
        clearSelectedRadioGroup()
        isLargeItemClicked = false
        selectedRadioGroup(checkedId)
      }
      selectedSmallItem()
    }

    for (i in 0..7) {
      val groupMinor = minorGroup[i]
      groupMinor.setOnCheckedChangeListener { _, checkedId ->
        selectedMinorRadioGroup(checkedId)
        selectedSmallItem()
      }
    }

    RxTextView.textChanges(comment_edittext).subscribe {
      count_textview.text = "${it.length} / 250"
    }
    RxView.clicks(delete_button)
      .map { if (!isSmallItemClicked) infoDialog(this, getString(R.string.delete_user_error_not_select)) }
      .filter { isSmallItemClicked }
      .throttleFirst(1, TimeUnit.SECONDS).subscribe {
      val user = service.currentUser!!
      if (user.bonusPoint >= 100) {
        val dialog = DeleteAlertDialog.newInstance(
          getString(R.string.delete_user_confirm_title),
          getString(R.string.delete_user_confirm_message),
          getString(R.string.delete_user_confirm_point, user.bonusPoint),
          getString(R.string.delete_user_confirm_ok),
          getString(R.string.cancel))
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "delete")
      } else {
        deleteAccount()
      }
    }
  }

  private fun deleteAccount() {
    val choice = when (reason_radio_group.checkedRadioButtonId) {
      reason2_radio.id -> DeleteReason.KEY_NOT_SELL
      else -> when (reason1_radio_group.checkedRadioButtonId) {
        minor1_radio.id -> DeleteReason.KEY_NO_PURPOSE_BOOK
        minor2_radio.id -> DeleteReason.KEY_FEW_PRODUCTS
        minor3_radio.id -> DeleteReason.KEY_FEW_FAVORITE_CATEGORIES
        minor4_radio.id -> DeleteReason.KEY_HIGH_PRICED_BOOK
        minor5_radio.id -> DeleteReason.KEY_OTHERS_MINOR1
        else -> when (reason3_radio_group.checkedRadioButtonId) {
          minor6_radio.id -> DeleteReason.KEY_USER_TROUBLE
          minor7_radio.id -> DeleteReason.KEY_UNKNOWN_PERSON
          minor8_radio.id -> DeleteReason.KEY_ATTACHED_BAD_EVALUATION
          minor9_radio.id -> DeleteReason.KEY_OTHERS_MINOR2
          else -> when (reason4_radio_group.checkedRadioButtonId) {
            minor10_radio.id -> DeleteReason.KEY_EXHIBITION_METHOD
            minor11_radio.id -> DeleteReason.KEY_PURCHASE_METHOD
            minor12_radio.id -> DeleteReason.KEY_DELIVERY
            minor13_radio.id -> DeleteReason.KEY_PRICING
            minor14_radio.id -> DeleteReason.KEY_PACKING
            minor15_radio.id -> DeleteReason.KEY_OTHERS_MINOR3
            else -> when (reason5_radio_group.checkedRadioButtonId) {
              minor16_radio.id -> DeleteReason.KEY_FEW_CHOICES
              minor17_radio.id -> DeleteReason.KEY_ANONYMOUS
              minor18_radio.id -> DeleteReason.KEY_NOT_SEARCH
              minor19_radio.id -> DeleteReason.KEY_MUCH_BUG
              minor20_radio.id -> DeleteReason.KEY_OTHERS_MINOR4
              else -> when (reason6_radio_group.checkedRadioButtonId) {
                minor21_radio.id -> DeleteReason.KEY_SOLD_OUT
                minor22_radio.id -> DeleteReason.KEY_BUSY
                minor23_radio.id -> DeleteReason.KEY_LITTLE_CAPACITY
                minor24_radio.id -> DeleteReason.KEY_GO_ABROAD
                minor25_radio.id -> DeleteReason.KEY_OTHERS_MINOR5
                else -> when (reason7_radio_group.checkedRadioButtonId) {
                  minor26_radio.id -> DeleteReason.KEY_MUCH_NEWS
                  minor27_radio.id -> DeleteReason.KEY_DISSATISFIED_ADMIN_EXCHANGE
                  minor28_radio.id -> DeleteReason.KEY_OTHERS_MINOR6
                  else -> when (reason8_radio_group.checkedRadioButtonId) {
                    minor29_radio.id -> DeleteReason.KEY_MERCARI
                    minor30_radio.id -> DeleteReason.KEY_KAURU
                    minor31_radio.id -> DeleteReason.KEY_FRIL
                    minor32_radio.id -> DeleteReason.KEY_RAKUMA
                    minor33_radio.id -> DeleteReason.KEY_MONOKYUN
                    minor34_radio.id -> DeleteReason.KEY_OTAMART
                    minor35_radio.id -> DeleteReason.KEY_OTHERS_MINOR7
                    else -> when (reason9_radio_group.checkedRadioButtonId) {
                      minor36_radio.id -> DeleteReason.KEY_LIMITED_FUNCTION
                      minor37_radio.id -> DeleteReason.KEY_BORED
                      minor38_radio.id -> DeleteReason.KEY_OTHERS_MINOR8
                      else -> 0
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    val progress = ProgressDialog(this)
    progress.setMessage(getString(R.string.loading))
    progress.setCancelable(false)
    val comment = if (comment_edittext.text.isNotBlank()) comment_edittext.text.toString() else null
    Single.just<Unit>(null)
      .doOnEach { progress.show() }
      .observeOn(Schedulers.newThread())
      .flatMap { service.users.deleteAccount(choice, comment) }
      .doOnEach { progress.dismiss() }
      .flatMap {
        RxAlertDialog.alert(this,
        getString(R.string.delete_user_success_title),
        getString(R.string.delete_user_success_message),
        getString(R.string.ok))
      }
      .subscribe({
        Timber.i("successfully delete account")
        tracker.trackAccountDelete()
        finish()
      }, {
        Timber.e(it, "delete account error")
        val error = BukumaError.errorType(it)
        if (error == BukumaError.Type.HttpError
          && error.errorCode == BukumaError.HTTP_ERROR_FORBIDDEN) {
          infoDialog(this, getString(R.string.delete_user_error_not_allow))
        } else {
          infoDialog(this, getString(R.string.error_tryagain))
        }
      })
  }

  // hide sub radio group
  fun hideRadioGroup() {
    for (i in 0..7) {
      val groupMinor = minorGroup[i]
      groupMinor.visibility = View.GONE
    }
  }

  fun selectedRadioGroup(checkedId: Int) {
    if (checkedId == majorItems[0].id) {
      minorGroup[0].visibility = View.VISIBLE
      selectedMinorGroupShow()
    } else if (checkedId == majorItems[1].id) {
      isSmallItemClicked = true
    } else {
      for (i in 2..8) {
        if (checkedId == majorItems[i].id) {
          minorGroup[i - 1].visibility = View.VISIBLE
          selectedMinorGroupShow()
          return
        } else {
          hideRadioGroup()
        }
      }
    }
  }

  fun selectedMinorRadioGroup(checkedId: Int) {
    for (i in 0..37) {
      isSmallItemClicked = minorItems[i].id != checkedId
    }
  }

  fun clearSelectedRadioGroup() {
    for (i in 0..7) {
      val groupMinor: RadioGroup = findViewById(minorGroup[i].id) as RadioGroup
      groupMinor.clearCheck()
    }
  }

  fun selectedSmallItem() {
    if (isSmallItemClicked) {
      delete_button.background = resources.getDrawable(R.drawable.background_button_primary)
    } else {
      delete_button.background = resources.getDrawable(R.drawable.background_button_delete)
    }
  }

  fun selectedMinorGroupShow() {
    isLargeItemClicked = true
    isSmallItemClicked = false
  }

  override fun onOkButtonClick() {
    deleteAccount()
  }
}