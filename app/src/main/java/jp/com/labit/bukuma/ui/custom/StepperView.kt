package jp.com.labit.bukuma.ui.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import jp.com.labit.bukuma.R

/**
 * Created by zoonooz on 10/25/2016 AD.
 * Step progress view
 */
class StepperView : View {

  var activeColor = 0
  var inactiveColor = 0
  var stepCount = 4

  private var current = 0 // start with step 1

  private var w = 0
  private var h = 0
  private var space = 0f
  private var dotRadius = 0
  private var stepPoints = emptyArray<PointF>()

  private val tempDotRectF = RectF()
  private val tempPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val tempLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)

  constructor(context: Context) : super(context)
  constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
    applyAttrs(attributeSet)
  }

  constructor(context: Context, attributeSet: AttributeSet, defStyle: Int) : super(context, attributeSet, defStyle) {
    applyAttrs(attributeSet)
  }

  private fun applyAttrs(attributeSet: AttributeSet) {
    val a = context.theme.obtainStyledAttributes(attributeSet, R.styleable.StepperView, 0, 0)
    try {
      activeColor = a.getColor(R.styleable.StepperView_activeColor, Color.BLACK)
      inactiveColor = a.getColor(R.styleable.StepperView_inactiveColor, Color.GRAY)
    } finally {
      a.recycle()
    }
  }

  fun setCurrentStep(step: Int) {
    if (step < stepCount) {
      current = step
      invalidate()
    }
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    this.w = w
    this.h = h
    dotRadius = h / 2
    tempLinePaint.strokeWidth = h / 3f
    space = w.toFloat() / stepCount

    stepPoints = emptyArray()
    for (i in 0..stepCount - 1) {
      stepPoints += PointF((space * i) + (space / 2), h / 2f)
    }
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    // line
    for (i in 0..stepPoints.size - 2) {
      tempLinePaint.color = if (i > current - 1) inactiveColor else activeColor
      canvas.drawLine((space * i) + (space / 2), h / 2f, (space * (i + 1)) + (space / 2), h / 2f, tempLinePaint)
    }

    // dot
    for (i in stepPoints.indices) {
      tempPaint.color = if (i > current) inactiveColor else activeColor
      val p = stepPoints[i]
      tempDotRectF.set(p.x - dotRadius, p.y - dotRadius, p.x + dotRadius, p.y + dotRadius)
      canvas.drawOval(tempDotRectF, tempPaint)
    }
  }
}