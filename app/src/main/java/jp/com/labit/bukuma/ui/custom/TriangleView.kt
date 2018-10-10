package jp.com.labit.bukuma.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View

/**
 * Created by zoonooz on 12/18/15 AD.
 * Chat triangle
 */

class TriangleView : View {

  private var color: Int = 0

  constructor(context: Context) : super(context) {
    init()
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
    init()
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init()
  }

  private fun init() {
    color = (background as ColorDrawable).color
    super.setBackgroundColor(Color.TRANSPARENT)
  }

  override fun setBackgroundColor(color: Int) {
    this.color = color
    super.setBackgroundColor(Color.TRANSPARENT)
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    canvas.drawPath(path(), paint())
  }

  private fun path(): Path {
    val w = width

    val path = Path()
    if (tag != null && tag == "right") {
      path.moveTo(0f, 0f)
      path.lineTo(w.toFloat(), 0f)
      path.lineTo(0f, w.toFloat())
      path.lineTo(0f, 0f)
    } else {
      path.moveTo(0f, 0f)
      path.lineTo(w.toFloat(), 0f)
      path.lineTo(w.toFloat(), w.toFloat())
      path.lineTo(0f, 0f)
    }
    path.close()

    return path
  }

  private fun paint(): Paint {
    val p = Paint()
    p.color = color
    return p
  }
}
