package jp.com.labit.bukuma.ui.activity.setting

import android.app.ProgressDialog
import android.os.Bundle
import android.view.Menu
import com.jakewharton.rxbinding.view.RxMenuItem
import com.jakewharton.rxbinding.widget.RxTextView
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.Response
import jp.com.labit.bukuma.extension.toCommaString
import jp.com.labit.bukuma.extension.toResponse
import jp.com.labit.bukuma.ui.activity.BaseActivity
import jp.com.labit.bukuma.util.RxAlertDialog
import jp.com.labit.bukuma.util.infoDialog
import kotlinx.android.synthetic.main.activity_money_confirm.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 9/30/2016 AD.
 * Confirm payout
 */
class MoneyConfirmActivity : BaseActivity() {

  companion object {
    val EXTRA_BANK_ACCT_ID = "extra_bank_account_id"
  }

  var bankAccountId = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_money_confirm)

    supportActionBar?.title = getString(R.string.title_money_confirm)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    sale_form.text = getString(R.string.money_unit, service.currentUser!!.point)

    RxTextView.textChanges(amount_form.inputView).subscribe {
      if (it.toString() == "¥") amount_form.text = "" // remove ¥ if there is no number
      else if (it.contains("¥") && !it.startsWith("¥")) amount_form.text = it.trimStart { it != '¥' }.toString() // remove non ¥ at start point
      else if (it.isNotEmpty() && !it.startsWith("¥")) amount_form.text = "¥$it" // add ¥
      else if (it.count() > 4 && it[it.count() - 4] != ',') amount_form.text = "¥" + try {
        it.toString().replace("¥", "").replace(",", "").toInt().toCommaString()
      } catch (ex: NumberFormatException) {
        it.toString()
      }
      amount_form.inputView.setSelection(amount_form.text.length)
    }

    bankAccountId = intent.getIntExtra(EXTRA_BANK_ACCT_ID, 0)
    if (bankAccountId == 0) throw IllegalStateException("Bank Account ID should not be zero")

    money_confirm_footer_text.text = getString(R.string.money_confirm_footer,
                                               service.config.needTransferFee,
                                               service.config.transferFee)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_done, menu)

    val progress = ProgressDialog(this)
    progress.setMessage(getString(R.string.loading))
    progress.setCancelable(false)
    RxMenuItem.clicks(menu.findItem(R.id.done))
        .throttleFirst(1, TimeUnit.SECONDS)
        .filter { validate() }
        .flatMap {
          val amount = amount_form.text.replace("¥", "").replace(",", "").toInt()
          RxAlertDialog.alert2(this, null,
              getString(R.string.money_confirm_confirm_message, amount),
              getString(R.string.money_confirm_confirm_ok),
              getString(R.string.cancel))
        }
        .filter { it }
        .doOnNext { progress.show() }
        .observeOn(Schedulers.newThread())
        .flatMap {
          val amount = amount_form.text.replace("¥", "").replace(",", "").toInt()
          service.api.withdrawToBankAccount(bankAccountId, amount).toObservable().toResponse()
        }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext {
          progress.dismiss()
          if (it is Response.Error) {
            Timber.e(it.error, "error withdraw to bank id $bankAccountId")
            infoDialog(this, getString(R.string.error_tryagain))
          }
        }
        .filter { it is Response.Success }
        .flatMap {
          RxAlertDialog.alert(this, null,
              getString(R.string.money_confirm_success_message),
              getString(R.string.ok)).toObservable()
        }
        .subscribe {
          Timber.i("success withdraw to bank id : $bankAccountId")
          finish()
        }

    return super.onCreateOptionsMenu(menu)
  }

  fun validate(): Boolean {
    val point = service.currentUser!!.point
    if (point == 0) {
      infoDialog(this, getString(R.string.money_confirm_error_zero_point))
      return false
    }

    val amount = try { amount_form.text.replace("¥", "").replace(",", "").toInt() }
    catch (ex: NumberFormatException) { 0 }

    if (amount == 0) {
      amount_form.error = getString(R.string.money_confirm_error_amount_empty)
      return false
    }

    if (amount < service.config.minApplicableAmount) {
      amount_form.error = getString(R.string.money_confirm_error_amount_too_low,
                                    service.config.minApplicableAmount)
      return false
    }

    if (amount > service.config.maxApplicableAmount) {
      amount_form.error = getString(R.string.money_confirm_error_amount_too_high,
                                    service.config.maxApplicableAmount)
      return false
    }

    if (amount > point) {
      amount_form.error = getString(R.string.money_confirm_error_amount_insufficient, point)
      return false
    }

    return true
  }
}
