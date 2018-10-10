package jp.com.labit.bukuma.ui.activity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import com.squareup.picasso.Picasso
import jp.com.labit.bukuma.BukumaApplication
import jp.com.labit.bukuma.BukumaConfig
import jp.com.labit.bukuma.BukumaPreference
import jp.com.labit.bukuma.analytic.BukumaAnalytic
import jp.com.labit.bukuma.api.BukumaService
import jp.com.labit.bukuma.manager.MaintenanceManager
import jp.com.labit.bukuma.ui.dialog.MaintenanceDialog
import jp.com.labit.bukuma.util.hideKeyboard
import rx.Subscription
import javax.inject.Inject

/**
 * Created by zoonooz on 9/8/2016 AD.
 * Base class for all activity
 * - restrict the app to portrait
 * - inject the dependency objects
 * - extra logic to all activity screen
 */
abstract class BaseActivity : AppCompatActivity() {

  @Inject lateinit var service: BukumaService
  @Inject lateinit var config: BukumaConfig
  @Inject lateinit var preference: BukumaPreference
  @Inject lateinit var tracker: BukumaAnalytic
  @Inject lateinit var picasso: Picasso
  @Inject lateinit var maintenanceManager: MaintenanceManager

  var ignoreMaintenance = false
  var maintenanceSubscription: Subscription? = null

  companion object {
    private const val MAINTENANCE_DIALOG_TAG = "maintenance"
    const val EXTRA_IGNORE_MAINTENANCE = "extra_ignore_maintenance"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    BukumaApplication.mainComponent.inject(this)

    // force portrait
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    ignoreMaintenance = intent.extras?.getBoolean(EXTRA_IGNORE_MAINTENANCE) ?: false
  }

  override fun onResume() {
    super.onResume()

    if (!ignoreMaintenance) {
      updateMaintenanceDialog()
      maintenanceSubscription = maintenanceManager.maintenanceSubject.subscribe({
        updateMaintenanceDialog()
      })
    }
  }

  override fun onPause() {
    maintenanceSubscription?.unsubscribe()
    super.onPause()
  }

  override fun onSupportNavigateUp(): Boolean {
    ActivityCompat.finishAfterTransition(this)

    // return to main page if it is root
    if (isTaskRoot) {
      val intent = Intent(this, MainActivity::class.java)
      startActivity(intent)
    }

    return true
  }

  override fun finish() {
    // prevent the keyboard still appear after finishing the activity
    hideKeyboard(this)
    super.finish()
  }

  private fun updateMaintenanceDialog() {
    if (maintenanceManager.isUnderMaintenance) {
      showMaintenanceDialog()
    } else {
      hideMaintenanceDialog()
    }
  }

  private fun showMaintenanceDialog() {
    val dialog = supportFragmentManager.findFragmentByTag(MAINTENANCE_DIALOG_TAG)
    if (dialog != null) {
      return
    }
    MaintenanceDialog().show(supportFragmentManager, MAINTENANCE_DIALOG_TAG)
  }

  private fun hideMaintenanceDialog() {
    val dialog = supportFragmentManager.findFragmentByTag(MAINTENANCE_DIALOG_TAG) as? MaintenanceDialog
    dialog?.let { it.dismiss() }
  }

  override fun onSaveInstanceState(outState: Bundle?) {
    super.onSaveInstanceState(outState)
  }
}
