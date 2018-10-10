package jp.com.labit.bukuma.ui.custom

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import java.util.*

/**
 * Created by zoonooz on 10/14/2016 AD.
 * Like view
 */
class LikeView : View {

  private var unlikeColor = Color.LTGRAY
  private var likeColor = Color.parseColor("#F54B6D")
  private val mHeartPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val mParticlePaints: List<Paint>
  private val animationDuration = 500
  private var progress = 0f
  private var w = 0
  private var h = 0
  private var size = 0
  private lateinit var center: Point
  private lateinit var rectF: RectF
  private var tempRectF = RectF()
  private var tempRectF2 = RectF()
  private var tempPath = Path()

  var like = false

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

  init {
    mParticlePaints = Arrays.asList(
        getParticlePaint(Color.parseColor("#43D6A5")),
        getParticlePaint(Color.parseColor("#F7DA36")),
        getParticlePaint(Color.parseColor("#6AB0F3")),
        getParticlePaint(Color.parseColor("#AD38DE")),
        getParticlePaint(Color.parseColor("#43D6A5")),
        getParticlePaint(Color.parseColor("#F7DA36")),
        getParticlePaint(Color.parseColor("#6AB0F3")),
        getParticlePaint(Color.parseColor("#AD38DE")))
  }

  private fun setProgress(progress: Float) {
    this.progress = progress
    postInvalidate()
  }

  fun setLike(like: Boolean, animate: Boolean) {
    this.like = like
    if (like) {
      if (animate) {
        val animator = ObjectAnimator.ofFloat(this, "progress", 0f, 1f)
        animator.duration = animationDuration.toLong()
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
      } else {
        setProgress(1f)
      }
    } else {
      setProgress(0f)
    }
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    this.w = w
    this.h = h

    size = if (width > height) height else width
    center = Point(width / 2, height / 2)
    rectF = RectF((center.x - size / 2).toFloat(),
        (center.y - size / 2).toFloat(),
        (center.x + size / 2).toFloat(),
        (center.y + size / 2).toFloat())
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    // particle
    if (progress >= 0.3f) {
      val subProgress = (progress - 0.3f) / 0.7f
      var particleSize = size * 0.03f
      if (subProgress > 0.8) {
        val sizeProgress = (progress - 0.8f) / 0.2f
        particleSize -= particleSize * sizeProgress
      }

      val particleStartPadding = size * 0.2f
      val particleEndPadding = particleSize
      val particlePadding = particleStartPadding - (particleStartPadding - particleEndPadding) * subProgress

      tempRectF.set(rectF)
      val particleRect = tempRectF
      particleRect.inset(particlePadding, particlePadding)

      // particles #1
      var particle1Angle = -5f
      var particle2Angle = 8f
      for (i in 0..7) {
        val pointF1 = getPointAtCircleAngle(particleRect, particle1Angle)
        drawParticleAt(pointF1, particleSize, canvas, i)
        val pointF2 = getPointAtCircleAngle(particleRect, particle2Angle)
        drawParticleAt(pointF2, particleSize, canvas, i)
        particle1Angle += 45f
        particle2Angle += 45f
      }
    }

    // heart
    tempRectF.set(rectF)
    val heartRectF = tempRectF
    val heartPadding = size * 0.25f
    if (progress == 0f) {
      heartRectF.inset(heartPadding, heartPadding)
    } else if (progress < 0.2f) {
      val subProgress = progress / 0.2f
      val padding = heartRectF.width() / 2 * (1f - subProgress)
      heartRectF.inset(padding, padding)
    } else if (progress >= 0.2f && progress < 0.8f) {
      val subProgress = (progress - 0.2f) / 0.6f
      val overshootPadding = heartPadding * 1.2f * subProgress
      heartRectF.inset(overshootPadding, overshootPadding)
    } else {
      val subProgress = (progress - 0.8f) / 0.2f
      val resumePadding = heartPadding * (1 + 0.2f * (1f - subProgress))
      heartRectF.inset(resumePadding, resumePadding)
    }
    val gap = heartRectF.width() * 0.1f
    val path = tempPath
    path.reset()
    path.moveTo(center.x.toFloat(), heartRectF.bottom)
    tempRectF2.set(heartRectF.left, heartRectF.top, heartRectF.centerX() + gap, heartRectF.centerY() + gap)
    path.arcTo(tempRectF2, 135f, 175f)
    tempRectF2.set(heartRectF.centerX() - gap, heartRectF.top, heartRectF.right, heartRectF.centerY() + gap)
    path.arcTo(tempRectF2, 230f, 175f)
    path.lineTo(center.x.toFloat(), heartRectF.bottom)
    mHeartPaint.color = if (progress == 0f) unlikeColor else likeColor
    canvas.drawPath(path, mHeartPaint)
  }

  // utils

  private fun getParticlePaint(color: Int): Paint {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = color
    return paint
  }

  private fun getPointAtCircleAngle(rectF: RectF, angle: Float): PointF {
    val r = rectF.width() / 2f
    val x = r * Math.cos(Math.toRadians(angle.toDouble())).toFloat() + rectF.centerX()
    val y = r * Math.sin(Math.toRadians(angle.toDouble())).toFloat() + rectF.centerY()
    return PointF(x, y)
  }

  private fun drawParticleAt(pointF: PointF, radius: Float, canvas: Canvas, paintIndex: Int) {
    val pRectF = RectF(pointF.x - radius, pointF.y - radius, pointF.x + radius, pointF.y + radius)
    canvas.drawOval(pRectF, mParticlePaints[paintIndex])
  }
}
