package com.slide.unlock

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale


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
        initAttributes(attrs)
        initPaint()
    }

    /**
     * 解锁控件背景参数
     * @see unlockCallback 解锁结果回调
     * @see trackPaint 背景画笔
     * @see trackRoundCorner 背景圆角大小
     * @see trackBgColor 背景颜色
     * @see trackRectF 背景绘制区域
     * @see trackPath 背景绘制路径
     */
    private var unlockCallback: OnSlideUnlockCallback? = null
    private val trackPaint: Paint = Paint()
    private var trackRoundCorner: Float = 0f
    private var unlockLockText: String? = ""

    @ColorInt
    private var trackBgColor: Int = Color.WHITE
    private val trackRectF: RectF = RectF()
    private val trackPath: Path = Path()

    /**
     * 滑动解锁滑块参数
     * @see thumbSrcPaint 图片画笔
     * @see thumbPaint 画笔
     * @see thumbPath 绘制路径
     * @see thumbRectF 绘制范围
     * @see thumbShape 滑块图形，圆[ThumbShape.CIRCLE]/方形[ThumbShape.SQUARE]
     * @see thumbRoundCorner 滑块圆角大小，仅仅[thumbShape]为[ThumbShape.SQUARE]时有效
     * @see thumbWidth 滑块宽度
     * @see thumbLeftBorder  滑块绘制最左侧边界
     * @see thumbRightBorder 滑块绘制最右侧边界
     * @see thumbLeftX 滑块左侧其实X轴位置
     * @see duration 滑动解锁失败，滑块回弹动画时长
     * @see slidingDistance 滑块移动间距
     * @see slidingStarX 滑块按下时的起始X轴
     * @see thumbDrawSrc 最终绘制的按钮icon图片
     * @see thumbPadding 滑块内边距
     */
    private val thumbSrcPaint: Paint = Paint()
    private val thumbPaint: Paint = Paint()
    private val thumbPath: Path = Path()
    private val thumbRectF: RectF = RectF()

    @ColorInt
    private var thumbBgColor: Int = Color.RED
    private var thumbRoundCorner: Float = 0f
    private var thumbWidth: Float = 100f
    private var thumbLeftBorder: Float = 0f
    private var thumbRightBorder: Float = 0f
    private var thumbLeftX = 0f
    private var thumbSrc: Bitmap? = null
    private var thumbDrawSrc: Bitmap? = null
    private var thumbDrawSrcRectF: RectF = RectF()
    private var thumbPadding: Int = 0

    @ColorInt
    private var thumbSrcTint: Int = -1
    private var thumbShape: ThumbShape = ThumbShape.SQUARE
    private var duration: Int = 500
    private var slidingDistance = 0f
    private var slidingStarX = 0f
    private val animator: ValueAnimator by lazy {
        createThumbAnimator()
    }

    /**
     * 解锁失败，滑块回弹动画
     * @return ValueAnimator
     */
    protected open fun createThumbAnimator(): ValueAnimator = let {
        ValueAnimator.ofFloat(1f, 0f).apply {
            duration = this@SlideUnlockView.duration.toLong()
            addUpdateListener {
                thumbLeftX = thumbLeftBorder + slidingDistance * it.animatedValue as Float
                resetThumbPath()
                postInvalidate()
            }
        }
    }


    /**
     * 获取自定义属性
     * @param attrs AttributeSet?
     */
    private fun initAttributes(attrs: AttributeSet?) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.SlideUnlockView)
        trackBgColor = array.getColor(R.styleable.SlideUnlockView_trackBgColor, Color.WHITE)
        trackRoundCorner = array.getDimension(R.styleable.SlideUnlockView_trackRoundCorner, 0f)
        thumbBgColor = array.getColor(R.styleable.SlideUnlockView_thumbBgColor, Color.RED)
        thumbSrcTint = array.getColor(R.styleable.SlideUnlockView_thumbSrcTint, -1)
        val drawable = array.getDrawable(R.styleable.SlideUnlockView_thumbSrc)
        thumbWidth = array.getDimension(R.styleable.SlideUnlockView_thumbWidth, 60f)
        duration = array.getInt(R.styleable.SlideUnlockView_duration, 500)
        unlockLockText = array.getString(R.styleable.SlideUnlockView_unlockLockText)
        thumbShape = array.getInt(R.styleable.SlideUnlockView_shapeType, 0).let {
            ThumbShape.parse(it)
        }
        check(drawable != null) {
            "The thumbSrc property must be set!!"
        }
        Log.d("thumbSrcTint:", "${thumbSrcTint}")
        if (thumbSrcTint != -1) {
            drawable.setTint(thumbSrcTint)
        }
        DrawableCompat.setTint(drawable, Color.RED)
        thumbSrc = drawable.toBitmap(config = Bitmap.Config.ARGB_8888)
        array.recycle()
    }


    /**
     * 画笔配置
     */
    protected open fun initPaint() {
        initTrackPaint()
        initThumbPaint()
    }

    /**
     * 背景画笔配置
     */
    protected open fun initTrackPaint() {
        trackPaint.style = Paint.Style.FILL
        trackPaint.strokeJoin = Paint.Join.ROUND
        trackPaint.strokeCap = Paint.Cap.ROUND
        trackPaint.color = trackBgColor
        trackPaint.isAntiAlias = true
        trackPaint.isDither = true
    }

    /**
     * 滑块画笔配置
     */
    protected open fun initThumbPaint() {
        thumbPaint.style = Paint.Style.FILL
        thumbPaint.strokeJoin = Paint.Join.ROUND
        thumbPaint.strokeCap = Paint.Cap.ROUND
        thumbPaint.color = thumbBgColor
        thumbPaint.isAntiAlias = true
        thumbPaint.isDither = true
        thumbSrcPaint.style = Paint.Style.FILL
        thumbSrcPaint.strokeJoin = Paint.Join.ROUND
        thumbSrcPaint.strokeCap = Paint.Cap.ROUND
        if (thumbSrcTint != null) {
            thumbSrcPaint.colorFilter = PorterDuffColorFilter(thumbSrcTint, PorterDuff.Mode.SRC_IN)
        }
        thumbSrcPaint.isAntiAlias = true
        thumbSrcPaint.isDither = true
    }


    /**
     * 测量，确定控件大小
     * @param widthMeasureSpec Int
     * @param heightMeasureSpec Int
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measureSize(widthMeasureSpec, 300)
        val height = measureSize(heightMeasureSpec, 60)
        setMeasuredDimension(width, height)
    }

    /**
     * 测量确定View大小
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

    /**
     * View大小变化回调
     * @param w Int
     * @param h Int
     * @param oldw Int
     * @param oldh Int
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initPath()
    }

    /**
     * View初始画质路径配置
     */
    private fun initPath() {
        thumbLeftBorder = paddingLeft.toFloat()
        thumbRightBorder = width - paddingRight.toFloat()
        thumbLeftX = thumbLeftBorder
        resetTrackPath()
        resetThumbPath()
    }

    /**
     * 解锁控件背景绘制路径配置
     */
    private fun resetTrackPath() {
        trackPath.reset()
        trackRectF.left = 0f
        trackRectF.right = width.toFloat()
        trackRectF.top = 0f
        trackRectF.bottom = height.toFloat()
        trackRoundCorner = height.toFloat()
        trackPath.addRoundRect(trackRectF, trackRoundCorner, trackRoundCorner, Path.Direction.CW)
    }

    /**
     * 解锁控件滑块绘制路径配置
     */
    private fun resetThumbPath() {
        thumbPath.reset()
        thumbRectF.left = thumbLeftX
        thumbRectF.top = paddingTop.toFloat()
        thumbRectF.bottom = thumbRectF.top + (height - paddingTop - paddingBottom)
        if (thumbShape == ThumbShape.CIRCLE) {
            resetCircleThumbPath()
        } else {
            resetSquareThumbPath()
        }
    }

    /**
     * 方形滑块绘制路径配置
     */
    private fun resetSquareThumbPath() {
        thumbRoundCorner = height.toFloat()
        thumbRectF.right = thumbRectF.left + thumbWidth
        thumbPath.addRoundRect(
            trackRectF,
            trackRoundCorner,
            trackRoundCorner,
            Path.Direction.CW
        )

        //确定图片位置和大小
        val iconSize: Float = thumbWidth - (thumbPadding * 2f)
        thumbDrawSrcRectF.left = thumbRectF.left + thumbPadding
        thumbDrawSrcRectF.right = thumbDrawSrcRectF.left + iconSize
        thumbDrawSrcRectF.top = thumbRectF.top + thumbPadding
        thumbDrawSrcRectF.top = thumbRectF.top + iconSize
        if (thumbDrawSrc == null) {
            thumbDrawSrc = thumbSrc?.scale(iconSize.toInt(), iconSize.toInt(), true)
        }
    }

    /**
     * 圆形滑块，绘制路径配置
     */
    private fun resetCircleThumbPath() {
        //确定滑块位置和大小
        thumbWidth = thumbRectF.bottom - thumbRectF.top
        val radius = thumbWidth / 2
        val cx = thumbRectF.left + radius
        val cy = thumbRectF.top + radius
        thumbRectF.right = thumbRectF.left + thumbWidth
        thumbPath.addCircle(cx, cy, radius, Path.Direction.CW)

        //确定图片位置和大小
        val iconSize: Float = thumbWidth - (thumbPadding * 2f)
        thumbDrawSrcRectF.left = thumbRectF.left + thumbPadding
        thumbDrawSrcRectF.right = thumbDrawSrcRectF.left + iconSize
        thumbDrawSrcRectF.top = thumbRectF.top + thumbPadding
        thumbDrawSrcRectF.top = thumbRectF.top + iconSize
        if (thumbDrawSrc == null) {
            thumbDrawSrc = thumbSrc?.scale(iconSize.toInt(), iconSize.toInt(), true)
        }
    }


    /**
     * 绘制图形
     * @param canvas Canvas
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPath(trackPath, trackPaint)
        canvas?.drawPath(thumbPath, thumbPaint)
        canvas?.drawBitmap(
            thumbDrawSrc!!,
            null,
            thumbDrawSrcRectF,
            thumbSrcPaint
        )
    }


    /**
     * 滑动事件拦截，处理滑动效果
     * @param event MotionEvent
     * @return Boolean
     */
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
                    unlockCallback?.onSlideUnlock(true)
                } else {
                    slidingDistance = thumbRectF.left - thumbLeftBorder
                    unlockCallback?.onSlideUnlock(false)
                    animator.start()
                }
            }
        }
        return true
    }


    /**
     * 根据手指滑动距离，移动滑块位置
     * @param event MotionEvent
     */
    private fun moveThumb(event: MotionEvent) {
        slidingDistance = event.x - slidingStarX
        thumbLeftX += slidingDistance
        if (thumbLeftX > thumbRightBorder - thumbWidth) {
            thumbLeftX = thumbRightBorder - thumbWidth
        }
        if (thumbLeftX < thumbLeftBorder) {
            thumbLeftX = thumbLeftBorder
        }
        slidingStarX = event.x
        resetThumbPath()
        postInvalidate()
    }


    /**
     * 重置View为初始状态值
     */
    open fun reset() {
        thumbLeftX = thumbLeftBorder
        resetThumbPath()
        postInvalidate()
    }


}