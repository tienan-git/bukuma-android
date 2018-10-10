package jp.com.labit.bukuma.ui.custom

import android.graphics.*
import com.squareup.picasso.Transformation

/**
 * Created by zoonooz on 11/25/2016 AD.
 * Round image transformer for Picasso
 */
class PicassoRoundTransform(val radius: Int) : Transformation {

  override fun transform(source: Bitmap): Bitmap {
    val paint = Paint()
    paint.isAntiAlias = true
    paint.shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

    val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    canvas.drawRoundRect(RectF(0f, 0f, source.width.toFloat(), source.height.toFloat()),
        radius.toFloat(), radius.toFloat(), paint)

    if (source != output) {
      source.recycle()
    }

    return output
  }

  override fun key() = "rounded"
}
