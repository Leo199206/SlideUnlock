package com.slide.unlock

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.toRect
import kotlin.math.abs


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
    protected open var unlockCallback: OnSlideUnlockCallback? = null
    protected open val trackPaint: Paint = Paint()

    @Px
    protected open var trackRoundCorner: Float = 0f

    @ColorInt
    protected open var trackBgColor: Int = Color.WHITE
    protected open val trackRectF: RectF = RectF()
    protected open val trackPath: Path = Path()

    /**
     * 解锁提示文字参数
     * @see unlockLockText 文字提示
     * @see unlockLockTextSize 文字大小
     * @see unlockLockTextColor 文字颜色
     */
    protected open var unlockLockText: String? = ""
    protected open var unlockLockTextSize: Int = 12

    @ColorInt
    protected open var unlockLockTextColor: Int = Color.WHITE
    protected open val textPaint = TextPaint()

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
     * @see iOSEffect 是否开启iOS风格效果
     */
    protected open val thumbSrcPaint: Paint = Paint()
    protected open val thumbPaint: Paint = Paint()
    protected open val thumbPath: Path = Path()
    protected open val thumbRectF: RectF = RectF()

    @ColorInt
    protected open var thumbBgColor: Int = Color.RED
    protected open var thumbRoundCorner: Float = 0f
    protected open var thumbWidth: Float = 100f
    protected open var thumbLeftBorder: Float = 0f
    protected open var thumbRightBorder: Float = 0f
    protected open var thumbLeftX: Float = 0f
    protected open var thumbSrc: Bitmap? = null
    protected open var thumbDrawSrc: Bitmap? = null
    protected open var thumbDrawSrcRectF: RectF = RectF()
    protected open var thumbPadding: Int = 0

    @ColorInt
    protected open var thumbSrcTint: Int = -1
    protected open var thumbShape: ThumbShape = ThumbShape.SQUARE
    protected open var duration: Int = 500
    protected open var slidingDistance: Float = 0f
    protected open var slidingStarX: Float = 0f
    protected open var iOSEffect: Boolean = false
    protected open val animator: ValueAnimator by lazy {
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
                thumbResilience(it.animatedValue as Float)
            }
        }
    }


    /**
     * 滑块回弹效果
     * @param value Float
     */
    protected open fun thumbResilience(value: Float) {
        thumbLeftX = thumbLeftBorder + slidingDistance * value
        resetThumbPath()
        if (iOSEffect) {
            resetTrackPath()
        }
        postInvalidate()
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
        thumbPadding = array.getDimensionPixelOffset(R.styleable.SlideUnlockView_thumbPadding, 0)
        duration = array.getInt(R.styleable.SlideUnlockView_duration, 500)
        iOSEffect = array.getBoolean(R.styleable.SlideUnlockView_iOSEffect, false)
        unlockLockText = array.getString(R.styleable.SlideUnlockView_unlockLockText)
        unlockLockTextColor =
            array.getColor(R.styleable.SlideUnlockView_unlockLockTextColor, Color.WHITE)
        unlockLockTextSize =
            array.getDimensionPixelSize(R.styleable.SlideUnlockView_unlockLockTextSize, 12)
        thumbShape = array.getInt(R.styleable.SlideUnlockView_shapeType, 0).let {
            ThumbShape.parse(it)
        }
        check(drawable != null) {
            "The thumbSrc property must be set!!"
        }
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
        initTextPaint()
    }


    /**
     * 字体画笔配置
     */
    protected open fun initTextPaint() {
        textPaint.textSize = unlockLockTextSize.toFloat()
        textPaint.isDither = true
        textPaint.isAntiAlias = true
        textPaint.color = unlockLockTextColor
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
    protected open fun resetTrackPath() {
        trackPath.reset()
        if (iOSEffect) {
            trackRectF.left = thumbLeftX - paddingLeft
        } else {
            trackRectF.left = 0f
        }
        trackRectF.right = width.toFloat()
        trackRectF.top = 0f
        trackRectF.bottom = height.toFloat()
        trackRoundCorner = height.toFloat()
        trackPath.addRoundRect(trackRectF, trackRoundCorner, trackRoundCorner, Path.Direction.CW)
    }

    /**
     * 解锁控件滑块绘制路径配置
     */
    protected open fun resetThumbPath() {
        thumbPath.reset()
        thumbRectF.left = thumbLeftX
        thumbRectF.top = paddingTop.toFloat()
        thumbRectF.bottom = height - paddingBottom.toFloat()
        if (thumbShape == ThumbShape.CIRCLE) {
            resetCircleThumbPath()
        } else {
            resetSquareThumbPath()
        }
    }

    /**
     * 方形滑块绘制路径配置
     */
    protected open fun resetSquareThumbPath() {
        thumbRoundCorner = height.toFloat()
        thumbRectF.right = thumbRectF.left + thumbWidth
        thumbPath.addRoundRect(
            thumbRectF,
            trackRoundCorner,
            trackRoundCorner,
            Path.Direction.CW
        )


        //确定图片位置和大小
        val iconWidth: Float = thumbRectF.right - thumbRectF.left - thumbPadding * 2
        val iconHeight: Float = thumbRectF.bottom - thumbRectF.top - thumbPadding * 2
        val thumbHeight = thumbRectF.height()
        thumbSrc?.also {
            val scale = calculateScale(it, iconWidth, iconHeight)
            val srcWidth: Float = it.width * scale
            val srcHeight: Float = it.height * scale
            thumbDrawSrcRectF.left = thumbRectF.left + (thumbWidth - srcWidth) / 2
            thumbDrawSrcRectF.right = thumbDrawSrcRectF.left + srcWidth
            thumbDrawSrcRectF.top = thumbRectF.top + (thumbHeight - srcHeight) / 2
            thumbDrawSrcRectF.bottom = thumbDrawSrcRectF.top + srcHeight
            if (thumbDrawSrc == null) {
                thumbDrawSrc = thumbSrc?.scale(srcWidth.toInt(), srcHeight.toInt(), true)
            }

        }
    }

    /**
     * 计算图片缩放比例
     * @param bitmap Bitmap
     * @param thumbWidth Float
     * @param thumbHeight Float
     */
    protected open fun calculateScale(
        bitmap: Bitmap,
        thumbWidth: Float,
        thumbHeight: Float
    ): Float = let {
        val scaleW: Float = if (bitmap.width > thumbWidth) {
            thumbWidth / bitmap.width
        } else {
            1f
        }
        val scaleH = if (bitmap.height > thumbHeight) {
            thumbHeight / bitmap.height
        } else {
            1f
        }
        if (scaleW > scaleH) {
            scaleH
        } else {
            scaleW
        }
    }


    /**
     * 圆形滑块，绘制路径配置
     */
    protected open fun resetCircleThumbPath() {
        //确定滑块位置和大小
        thumbWidth = thumbRectF.height()
        val radius = thumbWidth / 2
        val cx = thumbRectF.left + radius
        val cy = thumbRectF.top + radius
        thumbRectF.right = thumbRectF.left + thumbWidth
        thumbPath.addCircle(cx, cy, radius, Path.Direction.CW)

        //确定图片位置和大小
        var iconSize: Float = thumbWidth - (thumbPadding * 2f)
        (iconSize / 2).let {
            thumbDrawSrcRectF.left = cx - it
            thumbDrawSrcRectF.right = cx + it
            thumbDrawSrcRectF.top = cy - it
            thumbDrawSrcRectF.bottom = cy + it
            if (thumbDrawSrc == null) {
                thumbDrawSrc = thumbSrc?.scale(iconSize.toInt(), iconSize.toInt(), true)
            }
        }
    }


    /**
     * 绘制图形
     * @param canvas Canvas
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPath(trackPath, trackPaint)
        onDrawText(canvas)
        canvas?.drawPath(thumbPath, thumbPaint)
        canvas?.drawBitmap(
            thumbDrawSrc!!,
            null,
            thumbDrawSrcRectF,
            thumbSrcPaint
        )
    }

    /**
     * 解锁提示文字
     * @param canvas Canvas?
     */
    protected open fun onDrawText(canvas: Canvas?) {
        val textWidth = textPaint.measureText(unlockLockText)
        val drawX = (width - textWidth) * 0.5f
        val textHeight = (abs(textPaint.ascent()) - textPaint.descent()) / 2
        val drawY = height * 0.5f + textHeight / 2
        canvas?.drawText(unlockLockText.orEmpty(), drawX, drawY, textPaint)
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
    protected open fun moveThumb(event: MotionEvent) {
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
        if (iOSEffect) {
            resetTrackPath()
        }
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