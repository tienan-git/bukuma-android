package jp.com.labit.bukuma.ui.activity.setting

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.jakewharton.rxbinding.view.RxMenuItem
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.extension.Response
import jp.com.labit.bukuma.extension.isNotKatakana
import jp.com.labit.bukuma.extension.toResponse
import jp.com.labit.bukuma.model.Bank
import jp.com.labit.bukuma.ui.activity.BaseActivity
import jp.com.labit.bukuma.util.infoDialog
import jp.com.labit.bukuma.util.loadingDialog
import kotlinx.android.synthetic.main.activity_money_request.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by zoonooz on 9/30/2016 AD.
 * Input bank info to request payout
 */
class MoneyRequestActivity : BaseActivity() {

  var bank: Bank? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_money_request)

    supportActionBar?.title = getString(R.string.title_money_request)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    val progress = loadingDialog(this)
    service.api.getBankAccounts()
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnEach { progress.dismiss() }
        .subscribe({
          Timber.i("get bank account size : ${it.bank_accounts.size}")
          it.bank_accounts.firstOrNull()?.let { setDisplayBank(it) }
        }, {
          Timber.e(it, "error get bank account")
        })
  }

  fun setDisplayBank(bank: Bank) {
    this.bank = bank
    bank_form.text = bank.bankName
    branch_form.text = bank.branch
    accountno_form.text = bank.number
    type_form.text = bank.accountType

    // name
    val fullname = bank.nameKana.split(" ")
    if (fullname.isNotEmpty()) firstname_form.text = fullname[0]
    if (fullname.size > 1) lastname_form.text = fullname[1]
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val item = menu.add(R.string.next)
    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

    val progress = ProgressDialog(this)
    progress.setMessage(getString(R.string.loading))
    progress.setCancelable(false)
    RxMenuItem.clicks(item)
        .throttleFirst(1, TimeUnit.SECONDS)
        .filter { validate() }
        .doOnNext { progress.show() }
        .observeOn(Schedulers.newThread())
        .flatMap {
          val name = "${firstname_form.text} ${lastname_form.text}"
          if (bank != null) {
            Timber.i("bank exist -> update")
            service.api.updateBankAccount(
                bank!!.id, name, name,
                bank_form.text, branch_form.text,
                type_form.text, accountno_form.text).toObservable()
                .map { bank!!.id } // send instance bank id
                .toResponse()
          } else {
            Timber.i("no bank -> create")
            service.api.createBankAccount(
                name, name,
                bank_form.text, branch_form.text,
                type_form.text, accountno_form.text).toObservable()
                .map { it.bank_accounts.first().id }// send response bank id
                .toResponse()
          }
        }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnEach { progress.dismiss() }
        .subscribe {
          when (it) {
            is Response.Success -> {
              Timber.i("update or create bank success : $it")
              // go to confirm page
              val intent = Intent(this, MoneyConfirmActivity::class.java)
              intent.putExtra(MoneyConfirmActivity.EXTRA_BANK_ACCT_ID, it.value)
              startActivity(intent)
              finish()
            }
            is Response.Error -> {
              Timber.e("update or create bank error : $it")
              infoDialog(this, getString(R.string.error_tryagain))
            }
          }
        }

    return super.onCreateOptionsMenu(menu)
  }

  fun validate(): Boolean {
    var isOk = true

    // bank name
    if (bank_form.text.isBlank()) {
      isOk = false
      bank_form.error = getString(R.string.money_request_error_bank_empty)
    }

    // branch
    if (branch_form.text.isBlank()) {
      isOk = false
      branch_form.error = getString(R.string.money_request_error_branch_empty)
    } else if (branch_form.text.length != 3) {
      isOk = false
      branch_form.error = getString(R.string.money_request_error_branch_invalid)
    }

    // account number
    if (accountno_form.text.isBlank()) {
      isOk = false
      accountno_form.error = getString(R.string.money_request_error_accountno_empty)
    } else if (accountno_form.text.length != 7) {
      isOk = false
      accountno_form.error = getString(R.string.money_request_error_accountno_invalid)
    }

    // first name
    if (firstname_form.text.isBlank()) {
      isOk = false
      firstname_form.error = getString(R.string.money_request_error_firstname_empty)
    } else if (firstname_form.text.isNotKatakana()) {
      isOk = false
      firstname_form.error = getString(R.string.money_request_error_firstname_invalid)
    }

    // last name
    if (lastname_form.text.isBlank()) {
      isOk = false
      lastname_form.error = getString(R.string.money_request_error_lastname_empty)
    } else if (lastname_form.text.isNotKatakana()) {
      isOk = false
      lastname_form.error = getString(R.string.money_request_error_lastname_invalid)
    }

    return isOk
  }
}
