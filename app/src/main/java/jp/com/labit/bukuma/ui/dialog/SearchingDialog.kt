package jp.com.labit.bukuma.ui.dialog

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import jp.com.labit.bukuma.R
import jp.com.labit.bukuma.util.dpToPx

/**
 * Created by zoonooz on 10/7/2016 AD.
 * Book searching dialog
 */
class SearchingDialog : BaseDialog() {

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater?.inflate(R.layout.dialog_searching, container)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val focusView = view?.findViewById(R.id.focus_view) as? AnimationView
    focusView?.let {
      val animator = ObjectAnimator.ofFloat(it, "progress", 0f, 1f)
      animator.duration = 1000
      animator.interpolator = AccelerateDecelerateInterpolator()
      animator.repeatMode = ObjectAnimator.RESTART
      animator.repeatCount = ObjectAnimator.INFINITE
      animator.start()
    }
  }

  // view

  class AnimationView : View {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int) : super(context, attributeSet, defStyle)

    var progress = 0.0f
      set(value) {
        field = value
        postInvalidate()
      }

    var w = 0
    var h = 0

    // paints
    val circlePaint: Paint = {
      val paint = Paint(Paint.ANTI_ALIAS_FLAG)
      paint.style = Paint.Style.FILL
      paint.color = Color.parseColor("#B59E82")
      paint.strokeWidth = dpToPx(context.resources, 5f).toFloat()
      paint
    }()

    var size = 0
    lateinit var center: Point
    lateinit var rectF: RectF
    lateinit var clipPath: Path
    lateinit var bookRect: Rect
    lateinit var bookSmallRect: Rect
    val tempRect = Rect()

    val book: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.an_search_book_large)
    val bookSmall: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.an_search_book_small)

    init {
      setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
      super.onSizeChanged(w, h, oldw, oldh)
      this.w = w
      this.h = h
      size = if (w > h) h else w
      center = Point(w / 2, h / 2)
      rectF = RectF(
          (center.x - size / 2).toFloat(),
          (center.y - size / 2).toFloat(),
          (center.x + size / 2).toFloat(),
          (center.y + size / 2).toFloat())

      clipPath = Path()
      val insetRect = RectF(rectF)
      val insetSize = dpToPx(context.resources, 3f).toFloat()
      insetRect.inset(insetSize, insetSize)
      clipPath.addOval(insetRect, Path.Direction.CW)

      bookRect = Rect(
          (center.x - size / 3.8).toInt(),
          (center.y - size / 4.2).toInt(),
          (center.x + size / 3.8).toInt(),
          (center.y + size / 4.2).toInt())

      bookSmallRect = Rect(
          (center.x - size / 5.8).toInt(),
          (center.y - size / 6.2).toInt(),
          (center.x + size / 5.8).toInt(),
          (center.y + size / 6.2).toInt())
    }

    override fun onDraw(canvas: Canvas) {
      super.onDraw(canvas)
      val bookMargin = (size * 0.7).toInt()
      val offsetSmall = (progress * bookMargin).toInt()

      // draw small book right
      for (i in 0..4) {
        val offset = (progress * size).toInt()
        val margin = if (i > 0) size + ((i - 1) * bookMargin + offsetSmall) else offset
        tempRect.set(bookSmallRect)
        tempRect.offset(margin, 0)
        canvas.drawBitmap(bookSmall, null, tempRect, null)
      }

      // draw small book left
      for (i in 0..3) {
        val offset = ((1 - progress) * size).toInt()
        val margin = if (i > 0) size + ((i) * bookMargin - offsetSmall) else offset
        tempRect.set(bookSmallRect)
        tempRect.offset(-margin, 0)
        canvas.drawBitmap(bookSmall, null, tempRect, null)
      }

      // draw circle
      canvas.drawOval(rectF, circlePaint)

      // clip to circle
      canvas.clipPath(clipPath)

      // draw book
      tempRect.set(bookRect)
      tempRect.offset((progress * size).toInt(), 0)
      canvas.drawBitmap(book, null, tempRect, null)

      tempRect.set(bookRect)
      tempRect.offset(-((1 - progress) * size).toInt(), 0)
      canvas.drawBitmap(book, null, tempRect, null)
    }
  }
}
