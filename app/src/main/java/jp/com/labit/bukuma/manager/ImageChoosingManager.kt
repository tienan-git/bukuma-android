package jp.com.labit.bukuma.manager

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import jp.com.labit.bukuma.BuildConfig
import jp.com.labit.bukuma.R
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by zoonooz on 9/23/2016 AD.
 * Class to manager image choosing
 */
class ImageChoosingManager(val context: Context) {

  companion object {
    val REQUEST_CODE = 9999
  }

  /** chose image path */
  var pendingImagePath: String? = null

  /**
   * Show image chooser options intent
   * @return [Intent] to use for showing chooser
   */
  fun launchPhotoChooserIntent(): Intent? {
    val camPermission = Manifest.permission.CAMERA
    val galPermission = Manifest.permission.READ_EXTERNAL_STORAGE

    val camGranted = ContextCompat.checkSelfPermission(context, camPermission) == PackageManager.PERMISSION_GRANTED
    val galGranted = ContextCompat.checkSelfPermission(context, galPermission) == PackageManager.PERMISSION_GRANTED

    val intents = mutableListOf<Intent>()
    if (galGranted) intents.add(getGalleryIntent())
    if (isCameraSupported(context) && camGranted) {
      val cameraIntent = getCameraIntent()
      if (cameraIntent != null) {
        intents.add(cameraIntent)
      }
    }

    if (intents.size > 0) {
      val chooserIntent = Intent.createChooser(intents[0], context.getString(R.string.choose_photo))
      if (intents.size > 1) {
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(intents[1]))
      }
      return chooserIntent
    }
    return null
  }

  /**
   * Must call this from [Activity] that use this manager to let it handle incoming data.
   *
   * @param requestCode request code
   * @param resultCode forwarded result code
   * @param data forwarded data
   */
  fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == REQUEST_CODE) {
      if (resultCode == Activity.RESULT_OK) {
        val path: String?
        // if data enable use data
        if (data != null && data.data != null) {
          path = data.data.toString()
        } else {
          // if not use pre-define imageUri
          path = pendingImagePath
        }

        // clear previous path
        pendingImagePath = null

        path?.let {
          var file: File? = null
          // check for content path
          if (it.startsWith("content:")) {
            val pfd = context.contentResolver.openFileDescriptor(Uri.parse(it), "r")
            val fd = pfd?.fileDescriptor
            fd?.let {
              file = createImageFile()
              val input = FileInputStream(fd)
              val output = FileOutputStream(file)
              var read: Int
              val bytes = ByteArray(4096)
              while (true) {
                read = input.read(bytes)
                if (read == -1) {
                  break
                }
                output.write(bytes, 0, read)
              }
              input.close()
              output.close()
            }
            pfd?.close()
          } else {
            file = File(it)
          }

          file?.let {
            // update path
            pendingImagePath = it.absolutePath
            // rotate resize
            val bitmap = optimize(it.absolutePath)
            val output = FileOutputStream(it)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 80, output)
          }
        }
      } else {
        pendingImagePath = null
      }
    }
  }

  // Private utils -----

  private fun getCameraIntent(): Intent? {
    if (isCameraSupported(context)) {
      try {
        val file = createImageFile()
        pendingImagePath = file.absolutePath
        val photoURI = FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + ".provider",
            file)
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

        // grant permission (fix for 4.x device)
        val resInfoList = context.packageManager.queryIntentActivities(
            takePhotoIntent,
            PackageManager.MATCH_DEFAULT_ONLY)
        resInfoList.forEach { resolveInfo ->
          val packageName = resolveInfo.activityInfo.packageName
          context.grantUriPermission(packageName,
              photoURI,
              Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return takePhotoIntent
      } catch (ex: Exception) {
        // cannot create photo, some said no sd card
        Timber.e(ex, "cannot create photo file")
      }
    }
    return null
  }

  private fun getGalleryIntent(): Intent {
    val pickPhotoIntent = Intent(Intent.ACTION_GET_CONTENT)
    pickPhotoIntent.type = "image/*"
    pickPhotoIntent.addCategory(Intent.CATEGORY_OPENABLE)
    return pickPhotoIntent
  }

  private fun isCameraSupported(context: Context): Boolean {
    return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
  }

  // --------------

  @Throws(IOException::class)
  private fun createImageFile(): File {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val storageDir = File(context.cacheDir.path)
    val image = File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        storageDir      /* directory */)
    return image
  }

  /**
   * Fix image rotation and limit size
   */
  private fun optimize(photoPath: String): Bitmap? {
    try {
      val MAX_HEIGHT = 1024
      val MAX_WIDTH = 1024

      Timber.i("resize and rotate image : $photoPath")

      // First decode with inJustDecodeBounds=true to check dimensions
      val options = BitmapFactory.Options()
      options.inJustDecodeBounds = true
      BitmapFactory.decodeFile(photoPath, options)

      // Calculate inSampleSize
      options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, MAX_WIDTH, MAX_HEIGHT)

      // Decode bitmap with inSampleSize set
      options.inJustDecodeBounds = false
      val bitmap = BitmapFactory.decodeFile(photoPath, options)


      val ei = ExifInterface(photoPath)
      val orientation = ei.getAttributeInt(
          ExifInterface.TAG_ORIENTATION,
          ExifInterface.ORIENTATION_UNDEFINED)

      when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> return rotateBitmapImage(bitmap, 90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> return rotateBitmapImage(bitmap, 180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> return rotateBitmapImage(bitmap, 270f)
      }

      return bitmap
    } catch (ignore: Exception) {
      return null
    }

  }

  private fun rotateBitmapImage(source: Bitmap, angle: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
  }

  private fun calculateInSampleSize(rawWidth: Int, rawHeight: Int, reqWidth: Int, reqHeight: Int): Int {
    // Raw height and width of image
    val height = rawHeight
    val width = rawWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {

      // Calculate ratios of height and width to requested height and width
      val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
      val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())

      // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
      // with both dimensions larger than or equal to the requested height and width.
      inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio

      // This offers some additional logic in case the image has a strange
      // aspect ratio. For example, a panorama may have a much larger
      // width than height. In these cases the total pixels might still
      // end up being too large to fit comfortably in memory, so we should
      // be more aggressive with sample down the image (=larger inSampleSize).

      val totalPixels = (width * height).toFloat()

      // Anything more than 2x the requested pixels we'll sample down further
      val totalReqPixelsCap = reqWidth * reqHeight * 2.toFloat()

      while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
        inSampleSize++
      }
    }
    return inSampleSize
  }
}
