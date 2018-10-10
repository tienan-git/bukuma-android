package jp.com.labit.bukuma.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import jp.com.labit.bukuma.BukumaApplication
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.manager.ImageChoosingManager
import jp.com.labit.bukuma.util.RxAlertDialog
import javax.inject.Inject

/**
 * Created by zoonooz on 9/26/2016 AD.
 * Base activity provide image chooser logic
 */
abstract class BaseImageChooserActivity : BaseActivity() {

  @Inject lateinit var imageChooser: ImageChoosingManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    BukumaApplication.mainComponent.inject(this)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    imageChooser.onActivityResult(requestCode, resultCode, data)
    imageChooser.pendingImagePath?.let { onGotImage(it) }
  }

  /**
   * Start the choose image flow
   */
  fun chooseImage() {
    // permission
    val camPermission = Manifest.permission.CAMERA
    val galPermission = Manifest.permission.READ_EXTERNAL_STORAGE

    val camGranted = ContextCompat.checkSelfPermission(this, camPermission) == PackageManager.PERMISSION_GRANTED
    val galGranted = ContextCompat.checkSelfPermission(this, galPermission) == PackageManager.PERMISSION_GRANTED
    val ungranteds = mutableListOf<String>()
    if (!camGranted) ungranteds.add(camPermission)
    if (!galGranted) ungranteds.add(galPermission)

    if (ungranteds.size > 0) {
      // Should we show an explanation?
      val camRational = ActivityCompat.shouldShowRequestPermissionRationale(this, camPermission)
      val galRational = ActivityCompat.shouldShowRequestPermissionRationale(this, galPermission)

      if (camRational || galRational) {
        RxAlertDialog.alert(this, null, "need these", getString(R.string.ok)).subscribe {
          ActivityCompat.requestPermissions(this, ungranteds.toTypedArray(), 0)
        }
      } else {
        ActivityCompat.requestPermissions(this, ungranteds.toTypedArray(), 0)
      }
    } else {
      imageChooser.launchPhotoChooserIntent()?.let {
        startActivityForResult(it, ImageChoosingManager.REQUEST_CODE)
      }
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    if (grantResults.isNotEmpty() && grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
      imageChooser.launchPhotoChooserIntent()?.let {
        startActivityForResult(it, ImageChoosingManager.REQUEST_CODE)
      }
    }
  }

  // instance have to implement this to use image

  /**
   * Override this function to manager the path after image was chosen
   * @param path image path
   */
  abstract fun onGotImage(path: String)
}