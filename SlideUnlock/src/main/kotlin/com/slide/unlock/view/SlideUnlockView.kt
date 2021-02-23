package com.slide.unlock.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import com.slide.unlock.OnSlideUnlockCallback
import com.slide.unlock.R
import com.slide.unlock.ThumbShape
import kotlin.math.abs


/**
 * <pre>
 *   @author : leo
 *   @time   : 2021/02/21
 *   @desc   : 滑动解锁效果
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
    protected open var unlockLockTextShineColor: Int = Color.WHITE

    @ColorInt
    protected open var unlockLockTextColor: Int = Color.WHITE
    protected open var textWidth: Float = 0f
    protected open var drawTextX: Float = 0f
    protected open var textHeight: Float = 0f
    protected open var drawTextY: Float = 0f
    protected open val textPaint = TextPaint()
    protected open var shineDuration: Int = 3000
    protected open val shineAnimator: ValueAnimator by lazy {
        createShineAnimator()
    }
    protected open val gradientMatrix: Matrix by lazy { Matrix() }
    protected open var gradientTranslate: Float = 0f
    protected open val gradient: LinearGradient by lazy {
        LinearGradient(
            -width.toFloat(),
            0f,
            0f,
            0f,
            intArrayOf(unlockLockTextColor, unlockLockTextShineColor, unlockLockTextColor),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
    }

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
     * @see resilienceDuration 滑动解锁失败，滑块回弹动画时长
     * @see slidingDistance 滑块移动间距
     * @see slidingStarX 滑块按下时的起始X轴
     * @see thumbDrawSrc 最终绘制的按钮icon图片
     * @see thumbPadding 滑块内边距
     * @see shineEffect 是否开启iOS风格效果
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
    protected open var resilienceDuration: Int = 500
    protected open var slidingDistance: Float = 0f
    protected open var slidingStarX: Float = 0f
    protected open var shineEffect: Boolean = false
    protected open val springAnimator: ValueAnimator by lazy {
        createSpringAnimator()
    }


    /**
     * 字体闪光动效
     * @return ValueAnimator
     */
    protected open fun createShineAnimator(): ValueAnimator = let {
        ValueAnimator.ofFloat(-width.toFloat(), width.toFloat() * 2).apply {
            duration = this@SlideUnlockView.shineDuration.toLong()
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                setTextShineEffect(it.animatedValue as Float)
                postInvalidate()
            }
        }
    }


    /**
     * 设置文字闪光动画
     * @param value Float
     */
    private fun setTextShineEffect(value: Float) {
        gradientTranslate = value
        gradientMatrix.mapRect(RectF(0f, textWidth / 3, 0f, textHeight))
        gradientMatrix.setTranslate(gradientTranslate, 0f)
        gradient.setLocalMatrix(gradientMatrix)
    }

    /**
     * 设置滑块回弹动画
     * @return ValueAnimator
     */
    protected open fun createSpringAnimator(): ValueAnimator = let {
        ValueAnimator.ofFloat(1f, 0f).apply {
            duration = this@SlideUnlockView.resilienceDuration.toLong()
            addUpdateListener {
                setSpringEffect(it.animatedValue as Float)
                postInvalidate()
            }
        }
    }

    /**
     * 回弹效果更新
     * @param value Float
     */
    protected open fun setSpringEffect(value: Float) {
        thumbLeftX = thumbLeftBorder + slidingDistance * value
        resetThumbPath()
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
        resilienceDuration = array.getInt(R.styleable.SlideUnlockView_resilienceDuration, 500)
        shineEffect = array.getBoolean(R.styleable.SlideUnlockView_shineEffect, false)
        unlockLockText = array.getString(R.styleable.SlideUnlockView_unlockLockText)
        unlockLockTextColor =
            array.getColor(R.styleable.SlideUnlockView_unlockLockTextColor, Color.WHITE)
        unlockLockTextSize =
            array.getDimensionPixelSize(R.styleable.SlideUnlockView_unlockLockTextSize, 12)
        unlockLockTextShineColor =
            array.getColor(R.styleable.SlideUnlockView_unlockLockTextShineColor, Color.WHITE)
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
        if (shineEffect) {
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
     * 滑块绘制路径
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
     * 方形滑块绘制路径
     */
    protected open fun resetSquareThumbPath() {
        //确定滑块绘制坐标和范围
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
     * 圆形滑块绘制路径
     */
    protected open fun resetCircleThumbPath() {
        //确定滑块绘制坐标和范围
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
     * 绘制背景
     * @param canvas Canvas
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPath(trackPath, trackPaint)
        onDrawText(canvas)
    }

    /**
     * 绘制滑块
     * @param canvas Canvas
     */
    override fun onDrawForeground(canvas: Canvas?) {
        super.onDrawForeground(canvas)
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
        textWidth = textPaint.measureText(unlockLockText)
        drawTextX = (width - textWidth) * 0.5f
        textHeight = (abs(textPaint.ascent()) - textPaint.descent())
        drawTextY = (height * 0.5f + textHeight / 2)
        if (shineEffect && !shineAnimator.isRunning) {
            textPaint.shader = gradient
            shineAnimator.start()
        }
        canvas?.drawText(unlockLockText.orEmpty(), drawTextX, drawTextY, textPaint)
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
                setThumbScrollEffect(event)
                postInvalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                setSlideUnlockResult()
            }
        }
        return true
    }


    /**
     * 解锁状态回调
     * @param callback OnSlideUnlockCallback
     */
    fun setSlideUnlockCallback(callback: OnSlideUnlockCallback) {
        this.unlockCallback = callback
    }

    /**
     * 设置滑动解锁结果
     */
    protected open fun setSlideUnlockResult() {
        if (thumbRectF.right >= thumbRightBorder) {
            unlockCallback?.onSlideUnlock(true)
        } else {
            slidingDistance = thumbRectF.left - thumbLeftBorder
            unlockCallback?.onSlideUnlock(false)
            springAnimator.start()
        }
    }


    /**
     * 设置滑块移动位置效果
     * @param event MotionEvent
     */
    protected open fun setThumbScrollEffect(event: MotionEvent) {
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
    }


}