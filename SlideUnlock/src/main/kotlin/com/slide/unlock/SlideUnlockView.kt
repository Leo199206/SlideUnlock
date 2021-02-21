package com.slide.unlock

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

/**
 * <pre>
 *   @author : leo
 *   @time   : 2021/02/21
 *   @desc   : Slide to unlock
 * </pre>
 */
open class SlideUnlockView : View {
    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initPaint()
    }

    private var defaultWidth = 200
    private var defaultHeight = 30
    private val trackPaint: Paint = Paint()
    private val thumbPaint: Paint = Paint()
    private val trackPath: Path = Path()
    private val thumbPath: Path = Path()
    private val trackRectF: RectF = RectF()
    private val thumbRectF: RectF = RectF()
    private var trackRoundCorner: Float = 0f
    private var thumbRoundCorner: Float = 0f
    private var thumbWidth: Float = 100f
    private var thumbLeftBorder: Float = 0f
    private var thumbRightBorder: Float = 0f
    private var thumbStartX = 0f
    private var thumbShape: ThumbShape = ThumbShape.CIRCLE
    private var duration: Long = 500
    private val animator: ValueAnimator by lazy {
        createThumbAnimator()
    }

    protected open fun createThumbAnimator(): ValueAnimator {
        val valueAnimator = ValueAnimator.ofFloat(1f, 0f)
        valueAnimator.duration = duration
        valueAnimator.addUpdateListener {
            thumbStartX = thumbLeftBorder + slidingDistance * it.animatedValue as Float
            resetThumbPath()
            postInvalidate()
        }
        return valueAnimator
    }


    protected open fun initPaint() {
        initTrackPaint()
        initThumbPaint()
    }


    protected open fun initTrackPaint() {
        trackPaint.style = Paint.Style.FILL
        trackPaint.strokeJoin = Paint.Join.ROUND
        trackPaint.strokeCap = Paint.Cap.ROUND
        trackPaint.color = Color.RED
        trackPaint.isAntiAlias = true
        trackPaint.isDither = true
    }

    protected open fun initThumbPaint() {
        thumbPaint.style = Paint.Style.FILL
        thumbPaint.strokeJoin = Paint.Join.ROUND
        thumbPaint.strokeCap = Paint.Cap.ROUND
        thumbPaint.color = Color.GREEN
        thumbPaint.isAntiAlias = true
        thumbPaint.isDither = true
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measureSize(widthMeasureSpec, defaultWidth)
        val height = measureSize(heightMeasureSpec, defaultHeight)
        setMeasuredDimension(width, height)
    }

    /**
     * 测量尺寸
     *
     * @param measureSpec
     * @param defaultSize
     * @return
     */
    private fun measureSize(measureSpec: Int, defaultSize: Int): Int {
        var result: Int
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)
        if (mode == MeasureSpec.EXACTLY) {
            result = size
        } else {
            result = defaultSize
            if (mode == MeasureSpec.AT_MOST) result = result.coerceAtMost(size)
        }
        return result
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initPath()
    }


    private fun initPath() {
        thumbLeftBorder = paddingLeft.toFloat()
        thumbRightBorder = width - paddingRight.toFloat()
        thumbStartX = thumbLeftBorder
        resetTrackPath()
        resetThumbPath()
    }


    private fun resetTrackPath() {
        trackPath.reset()
        trackRectF.left = 0f
        trackRectF.right = width.toFloat()
        trackRectF.top = 0f
        trackRectF.bottom = height.toFloat()
        trackRoundCorner = height.toFloat()
        trackPath.addRoundRect(trackRectF, trackRoundCorner, trackRoundCorner, Path.Direction.CW)
    }

    private fun resetThumbPath() {
        thumbPath.reset()
        thumbRectF.left = thumbStartX
        thumbRectF.top = paddingTop.toFloat()
        thumbRectF.bottom = thumbRectF.top + (height - paddingTop - paddingBottom)
        if (thumbShape == ThumbShape.CIRCLE) {
            thumbWidth = thumbRectF.bottom - thumbRectF.top
            val radius = thumbWidth / 2
            val cx = thumbRectF.left + radius
            val cy = thumbRectF.top + radius
            thumbRectF.right = thumbRectF.left + thumbWidth
            thumbPath.addCircle(cx, cy, radius, Path.Direction.CW)
        } else {
            thumbRoundCorner = height.toFloat()
            thumbRectF.right = thumbRectF.left + thumbWidth
            thumbPath.addRoundRect(
                trackRectF,
                trackRoundCorner,
                trackRoundCorner,
                Path.Direction.CW
            )
        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPath(trackPath, trackPaint)
    }


    override fun onDrawForeground(canvas: Canvas?) {
        super.onDrawForeground(canvas)
        canvas?.drawPath(thumbPath, thumbPaint)
    }


    private var slidingDistance = 0f
    private var slidingStarX = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                slidingStarX = event.x
            }
            MotionEvent.ACTION_MOVE -> {
                moveThumb(event)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (thumbRectF.right >= thumbRightBorder) {
                    reset()
                } else {
                    slidingDistance = thumbRectF.left - thumbLeftBorder
                    animator.start()
                }
            }
        }
        return true
    }


    private fun moveThumb(event: MotionEvent) {
        slidingDistance = event.x - slidingStarX
        thumbStartX += slidingDistance
        if (thumbStartX > thumbRightBorder - thumbWidth) {
            thumbStartX = thumbRightBorder - thumbWidth
        }
        if (thumbStartX < thumbLeftBorder) {
            thumbStartX = thumbLeftBorder
        }
        slidingStarX = event.x
        resetThumbPath()
        postInvalidate()
    }


    private fun reset() {
        thumbStartX = thumbLeftBorder
        resetThumbPath()
        postInvalidate()
    }


}