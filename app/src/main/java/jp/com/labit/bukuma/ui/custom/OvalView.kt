package jp.com.labit.bukuma.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View

/**
 * Created by zoonooz on 9/13/2016 AD.
 * Oval view
 */
class OvalView : View {

  private var color: Int = 0
  private var paint: Paint? = null
  private var tempRectF = RectF()

  constructor(context: Context): super(context) {
    init()
  }

  constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
    init()
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int): super(context, attrs, defStyle) {
    init()
  }

  private fun init() {
    color = (background as ColorDrawable).color
    super.setBackgroundColor(Color.TRANSPARENT)

    paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint!!.color = color
  }

  override fun setBackgroundColor(color: Int) {
    this.color = color
    super.setBackgroundColor(Color.TRANSPARENT)

    paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint!!.color = color

    invalidate()
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    tempRectF.set(0f, 0f, w.toFloat(), h.toFloat())
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    canvas.drawOval(tempRectF, paint)
  }
}