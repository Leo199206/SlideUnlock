package com.slide.unlock.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import com.slide.unlock.*
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

    private var distanceY: Int = 0
    private var distanceX: Int = 0
    private var firstY: Int = 0
    private var firstX: Int = 0
    private val touchSlop: Int by lazy {
        ViewConfiguration.get(context).scaledTouchSlop
    }

    /**
     * 解锁控件背景参数
     * @see unlockCallback 解锁结果回调
     * @see trackPaint 背景画笔
     * @see trackRoundCorner 背景圆角大小
     * @see trackPadding 背景内边距
     * @see trackBgColor 背景颜色
     * @see trackRectF 背景绘制区域
     * @see trackPath 背景绘制路径
     */
    protected open var unlockCallback: OnSlideUnlockCallback? = null
    protected open val trackPaint: Paint = Paint()

    @Px
    protected open var trackRoundCorner: Float = 0f

    @Px
    protected open var trackPadding: Int = 0

    @ColorInt
    protected open var trackBgColor: Int = Color.WHITE
    protected open val trackRectF: RectF = RectF()
    protected open val trackPath: Path = Path()

    /**
     * 解锁提示文字参数
     * @see unlockLockText 文字提示
     * @see unlockTextSize 文字大小
     * @see unlockLockTextColor 文字颜色
     * @see unlockLockTextShineColor 文字流光字体颜色
     * @see unlockLockTextWidth 文字宽度
     * @see unlockLockTextHeight 文字高度
     * @see unlockLockTextPaint 提示文字画笔
     * @see unlockLockTextStyle 字体风格
     * @see unlockLockTextDrawY 提示文字绘制Y轴位置
     * @see shineDuration 流光字体动画时长
     * @see shineAnimator 流光属性动画操作对象
     * @see gradientMatrix 流光效果渐变位置变换
     * @see gradientTranslate 流光效果移动位置
     * @see gradient 流光渐变效果实现
     */
    protected open var unlockLockText: String? = ""
    protected open var unlockTextSize: Int = 12

    @ColorInt
    protected open var unlockLockTextShineColor: Int = Color.WHITE

    @ColorInt
    protected open var unlockLockTextColor: Int = Color.WHITE
    protected open var unlockLockTextWidth: Float = 0f
    protected open var unlockLockTextHeight: Float = 0f
    protected open var unlockLockTextDrawY: Float = 0f
    protected open val unlockLockTextPaint = TextPaint()
    protected open var unlockLockTextStyle: TextStyle = TextStyle.DEFAULT
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
     * @see thumbContentDrawablePaint 图片画笔
     * @see thumbBackgroundPaint 画笔
     * @see thumbBackgroundPath 绘制路径
     * @see thumbBackgroundRectF 绘制范围
     * @see thumbShape 滑块图形，圆[ThumbShape.CIRCLE]/方形[ThumbShape.SQUARE]
     * @see thumbBackgroundRoundCorner 滑块圆角大小，仅仅[thumbShape]为[ThumbShape.SQUARE]时有效
     * @see thumbBackgroundWidth 滑块宽度
     * @see thumbLeftBorder  滑块绘制最左侧边界
     * @see thumbRightBorder 滑块绘制最右侧边界
     * @see thumbLeftX 滑块左侧起始X轴位置
     * @see thumbRightX 滑块右侧结束X轴位置
     * @see thumbStartLeftX 滑块默认左侧起始X轴位置
     * @see thumbStartRightX 滑块默认右侧起始X轴位置
     * @see resilienceDuration 滑动解锁失败，滑块回弹动画时长
     * @see slidingDistance 滑块移动间距
     * @see slidingStarX 滑块按下时的起始X轴
     * @see thumbDrawDrawable 最终绘制的按钮icon图片
     * @see thumbPadding 滑块内边距
     * @see shineEffect 是否开启iOS风格效果
     * @see thumbContentTextWidth 滑块文字宽度
     * @see thumbContentTextDrawY 滑块文字绘制Y轴位置
     * @see thumbContentTextDrawX 滑块文字绘制X轴位置
     * @see thumbContentText 滑块文字
     * @see thumbContentTextHeight 滑块文字高度
     * @see thumbContentTexStyle 滑块文字风格
     * @see thumbType 滑块内容类型（文字/图标）
     */
    protected open val thumbContentDrawablePaint: Paint by lazy { Paint() }
    protected open val thumbContentTextPaint: Paint by lazy { TextPaint() }
    protected open val thumbBackgroundPaint: Paint = Paint()
    protected open val thumbBackgroundPath: Path = Path()
    protected open val thumbBackgroundRectF: RectF = RectF()
    protected open var thumbContentTextSize: Float = 10f
    protected open var thumbContentTextWidth: Float = 0f
    protected open var thumbContentTextDrawY: Float = 0f
    protected open var thumbContentTextDrawX: Float = 0f
    protected open var thumbContentTextHeight: Float = 0f
    protected open var thumbContentTexStyle: TextStyle = TextStyle.DEFAULT

    @ColorInt
    protected open var thumbBackgroundColor: Int = Color.RED
    protected open var thumbBackgroundRoundCorner: Float = 0f
    protected open var thumbBackgroundWidth: Float = 100f
    protected open var thumbLeftBorder: Float = 0f
    protected open var thumbRightBorder: Float = 0f
    protected open var thumbStartLeftX: Float = 0f
    protected open var thumbStartRightX: Float = 0f
    protected open var thumbLeftX: Float = 0f
    protected open var thumbRightX: Float = 0f
    protected open var thumbDrawable: Bitmap? = null
    protected open var thumbDrawDrawable: Bitmap? = null
    protected open val thumbDrawDrawableRectF: RectF by lazy { RectF() }
    protected open var thumbPadding: Int = 0
    protected open var thumbContentText: String = ""
    protected open var thumbType: ThumbType = ThumbType.DRAWABLE

    @ColorInt
    protected open var thumbTint: Int = -1
    protected open var thumbShape: ThumbShape = ThumbShape.SQUARE
    protected open var resilienceDuration: Int = 500
    protected open var slidingDistance: Float = 0f
    protected open var slidingStarX: Float = 0f
    protected open var slidingStarY: Float = 0f
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
        gradientMatrix.mapRect(RectF(0f, unlockLockTextWidth / 3, 0f, unlockLockTextHeight))
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
                setSlideUnlockProgress()
                resetThumbPath()
                postInvalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    slidingDistance = 0f
                }
            })
        }
    }

    /**
     * 回弹效果更新
     * @param value Float
     */
    protected open fun setSpringEffect(value: Float) {
        thumbLeftX = thumbLeftBorder + slidingDistance * value
        thumbRightX = thumbLeftX + thumbBackgroundWidth
    }


    /**
     * 获取自定义属性
     * @param attrs AttributeSet?
     */
    private fun initAttributes(attrs: AttributeSet?) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.SlideUnlockView)
        val count = array.indexCount
        var drawable: Drawable? = null
        for (index in 0 until count) {
            drawable = setAttributesValue(array.getIndex(index), array, drawable)
        }
        if (thumbType == ThumbType.DRAWABLE) {
            check(drawable != null) {
                "The thumbSrc property must be set!!"
            }
            if (thumbTint != -1) {
                drawable.setTint(thumbTint)
            }
            DrawableCompat.setTint(drawable, Color.RED)
            thumbDrawable = drawable.toBitmap(config = Bitmap.Config.ARGB_8888)
        }
        array.recycle()

    }


    /**
     * 设置自定义属性参数值
     * @param indexValue Int
     * @param array TypedArray
     * @param drawable Drawable?
     * @return Drawable?
     */
    private fun setAttributesValue(
        indexValue: Int,
        array: TypedArray,
        drawable: Drawable?
    ): Drawable? {
        var drawable1 = drawable
        when (indexValue) {
            R.styleable.SlideUnlockView_trackBgColor -> {
                trackBgColor =
                    array.getColor(indexValue, Color.WHITE)
            }
            R.styleable.SlideUnlockView_trackRoundCorner -> {
                trackRoundCorner =
                    array.getDimension(indexValue, 0f)
            }
            R.styleable.SlideUnlockView_thumbBgColor -> {
                thumbBackgroundColor =
                    array.getColor(indexValue, Color.RED)
            }
            R.styleable.SlideUnlockView_thumbPadding -> {
                thumbPadding =
                    array.getDimensionPixelOffset(indexValue, 0)
            }
            R.styleable.SlideUnlockView_thumbTint -> {
                thumbTint = array.getColor(indexValue, -1)
            }
            R.styleable.SlideUnlockView_thumbDrawable -> {
                drawable1 = array.getDrawable(indexValue)
            }
            R.styleable.SlideUnlockView_thumbWidth -> {
                thumbBackgroundWidth = array.getDimension(indexValue, 60f)
            }
            R.styleable.SlideUnlockView_thumbText -> {
                thumbContentText = array.getString(indexValue).orEmpty()
            }
            R.styleable.SlideUnlockView_thumbTextStyle -> {
                thumbContentTexStyle =
                    array.getInt(
                        indexValue,
                        TextStyle.DEFAULT.value.style
                    ).let {
                        TextStyle.parse(it)
                    }
            }
            R.styleable.SlideUnlockView_thumbType -> {
                thumbType =
                    array.getInt(
                        indexValue,
                        ThumbType.DRAWABLE.value
                    ).let {
                        ThumbType.parse(it)
                    }
            }
            R.styleable.SlideUnlockView_thumbShape -> {
                thumbShape =
                    array.getInt(indexValue, ThumbShape.CIRCLE.value).let {
                        ThumbShape.parse(it)
                    }
            }
            R.styleable.SlideUnlockView_resilienceDuration -> {
                resilienceDuration = array.getInt(indexValue, 500)
            }
            R.styleable.SlideUnlockView_unlockLockText -> {
                unlockLockText = array.getString(indexValue)
            }
            R.styleable.SlideUnlockView_unlockLockTextSize -> {
                unlockTextSize =
                    array.getDimensionPixelSize(indexValue, 12)
            }
            R.styleable.SlideUnlockView_unlockLockTextColor -> {
                unlockLockTextColor =
                    array.getColor(indexValue, Color.WHITE)
            }
            R.styleable.SlideUnlockView_unlockLockTextShineColor -> {
                unlockLockTextShineColor =
                    array.getColor(indexValue, Color.WHITE)
            }
            R.styleable.SlideUnlockView_unlockLockTextStyle -> {
                unlockLockTextStyle =
                    array.getInt(
                        indexValue,
                        TextStyle.DEFAULT.value.style
                    ).let {
                        TextStyle.parse(it)
                    }
            }
            R.styleable.SlideUnlockView_shineDuration -> {
                shineDuration = array.getInt(indexValue, 3000)
            }
            R.styleable.SlideUnlockView_shineEffect -> {
                shineEffect = array.getBoolean(indexValue, false)
            }
        }
        return drawable1
    }


    /**
     * 画笔配置
     */
    protected open fun initPaint() {
        initTrackPaint()
        initThumbPaint()
        initUnlockTextPaint()
    }


    /**
     * 解锁提示字体画笔配置
     */
    protected open fun initUnlockTextPaint() {
        unlockLockTextPaint.textSize = unlockTextSize.toFloat()
        unlockLockTextPaint.isDither = true
        unlockLockTextPaint.isAntiAlias = true
        unlockLockTextPaint.color = unlockLockTextColor
        unlockLockTextPaint.typeface = unlockLockTextStyle.value
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
        initThumbBackgroundPaint()
        initThumbContentPaint()
    }

    /**
     * 滑块内容画笔配置
     */
    private fun initThumbContentPaint() {
        if (thumbType == ThumbType.DRAWABLE) {
            initThumbContentDrawablePaint()
        } else {
            initThumbContentTextPaint()
        }
    }

    /**
     * 滑块内图片画笔配置
     */
    protected open fun initThumbContentDrawablePaint() {
        thumbContentDrawablePaint.style = Paint.Style.FILL
        thumbContentDrawablePaint.strokeJoin = Paint.Join.ROUND
        thumbContentDrawablePaint.strokeCap = Paint.Cap.ROUND
        if (thumbTint != null) {
            thumbContentDrawablePaint.colorFilter =
                PorterDuffColorFilter(thumbTint, PorterDuff.Mode.SRC_IN)
        }
        thumbContentDrawablePaint.isAntiAlias = true
        thumbContentDrawablePaint.isDither = true
    }

    /**
     * 滑块内容文字画笔配置
     */
    protected open fun initThumbContentTextPaint() {
        thumbContentTextPaint.style = Paint.Style.FILL
        thumbContentTextPaint.strokeJoin = Paint.Join.ROUND
        thumbContentTextPaint.strokeCap = Paint.Cap.ROUND
        thumbContentTextPaint.color = thumbTint
        thumbContentTextPaint.isAntiAlias = true
        thumbContentTextPaint.isDither = true
        thumbContentTextPaint.typeface = thumbContentTexStyle.value
    }

    /**
     * 滑块背景画笔配置
     */
    private fun initThumbBackgroundPaint() {
        thumbBackgroundPaint.style = Paint.Style.FILL
        thumbBackgroundPaint.strokeJoin = Paint.Join.ROUND
        thumbBackgroundPaint.strokeCap = Paint.Cap.ROUND
        thumbBackgroundPaint.color = thumbBackgroundColor
        thumbBackgroundPaint.isAntiAlias = true
        thumbBackgroundPaint.isDither = true
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
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)
        return when (mode) {
            MeasureSpec.AT_MOST -> {
                size or MEASURED_STATE_TOO_SMALL
            }
            MeasureSpec.EXACTLY -> {
                size
            }
            else -> {
                defaultSize
            }
        }
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
        initThumbContentTextPath()
        initUnlockLockTextPosition()
    }


    /**
     * 初始化解锁提示文字位置
     */
    private fun initUnlockLockTextPosition() {
        unlockLockTextWidth = unlockLockTextPaint.measureText(unlockLockText)
        unlockLockTextHeight = (abs(unlockLockTextPaint.ascent()) - unlockLockTextPaint.descent())
        unlockLockTextDrawY = (height + unlockLockTextHeight) * 0.5f
    }


    /**
     * View初始画质路径配置
     */
    private fun initPath() {
        thumbLeftBorder = paddingLeft.toFloat()
        thumbRightBorder = width - paddingRight.toFloat()
        thumbLeftX = thumbLeftBorder
        if (thumbShape == ThumbShape.CIRCLE) {
            thumbBackgroundWidth = (height - paddingTop - paddingBottom).toFloat()
        }
        thumbRightX = thumbLeftX + thumbBackgroundWidth
        thumbStartLeftX = thumbLeftX
        thumbStartRightX = thumbRightX
        resetTrackPath()
        resetThumbPath()
    }


    /**
     * 初始化滑块中心内容文字大小
     */
    private fun initThumbContentTextPath() {
        if (thumbType != ThumbType.TEXT) {
            return
        }
        var thumbContentHeight = thumbBackgroundRectF.height() - thumbPadding * 2
        var thumbContentWidth = thumbBackgroundRectF.width() - thumbPadding * 2
        thumbContentTextPaint.textSize = thumbContentHeight
        thumbContentTextWidth = thumbContentTextPaint.measureText(thumbContentText)
        if (thumbContentTextWidth > thumbContentWidth) {
            val scale = thumbContentWidth / thumbContentTextWidth
            thumbContentTextPaint.textSize = thumbContentHeight * scale
            thumbContentTextWidth = thumbContentTextPaint.measureText(thumbContentText)
        }
        thumbContentTextSize = thumbContentTextPaint.textSize
        thumbContentTextHeight =
            (abs(thumbContentTextPaint.ascent()) - thumbContentTextPaint.descent())
        thumbContentTextDrawY = (height + thumbContentTextHeight) * 0.5f
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
        resetThumbBackgroundPath()
        resetThumbContentPath()
    }


    /**
     * 滑块中心内容配置
     */
    protected open fun resetThumbContentPath() {
        if (thumbType == ThumbType.TEXT) {
            // todo do noting
        } else {
            resetThumbDrawablePath()
        }
    }


    /**
     * 滑块背景图形路径配置
     */
    protected open fun resetThumbBackgroundPath() {
        thumbBackgroundPath.reset()
        thumbBackgroundRectF.left = thumbLeftX
        thumbBackgroundRectF.top = paddingTop.toFloat()
        thumbBackgroundRectF.bottom = height - paddingBottom.toFloat()
        if (thumbShape == ThumbShape.CIRCLE) {
            resetCircleThumbBackgroundPath()
        } else {
            resetSquareThumbBackgroundPath()
        }
    }


    /**
     * 方形滑块背景绘制路径
     */
    protected open fun resetSquareThumbBackgroundPath() {
        //确定滑块绘制坐标和范围
        thumbBackgroundRoundCorner = height.toFloat()
        thumbBackgroundRectF.right = thumbRightX
        thumbBackgroundPath.addRoundRect(
            thumbBackgroundRectF,
            thumbBackgroundRoundCorner,
            thumbBackgroundRoundCorner,
            Path.Direction.CW
        )
    }


    /**
     * 圆形滑块背景绘制路径
     */
    protected open fun resetCircleThumbBackgroundPath() {
        //确定滑块绘制坐标和范围
        val radius = thumbBackgroundWidth / 2
        val cx = thumbBackgroundRectF.left + radius
        val cy = thumbBackgroundRectF.top + radius
        thumbBackgroundRectF.right = thumbRightX
        thumbBackgroundPath.addCircle(cx, cy, radius, Path.Direction.CW)
    }


    /**
     * 滑块图片绘制路径
     */
    protected open fun resetThumbDrawablePath() {
        if (thumbShape == ThumbShape.CIRCLE) {
            resetCircleThumbDrawablePath()
        } else {
            resetSquareThumbDrawablePath()
        }
    }

    /**
     * 滑块为方形时，图片绘制路径
     */
    private fun resetSquareThumbDrawablePath() {
        //确定图片位置和大小
        val iconWidth: Float =
            thumbBackgroundRectF.right - thumbBackgroundRectF.left - thumbPadding * 2
        val iconHeight: Float =
            thumbBackgroundRectF.bottom - thumbBackgroundRectF.top - thumbPadding * 2
        val thumbHeight = thumbBackgroundRectF.height()
        thumbDrawable?.also {
            val scale = calculateScale(it, iconWidth, iconHeight)
            val srcWidth: Float = it.width * scale
            val srcHeight: Float = it.height * scale
            thumbDrawDrawableRectF.right = thumbRightX - thumbBackgroundWidth / 2 + srcWidth / 2
            thumbDrawDrawableRectF.left = thumbDrawDrawableRectF.right - srcWidth
            thumbDrawDrawableRectF.top = thumbBackgroundRectF.top + (thumbHeight - srcHeight) / 2
            thumbDrawDrawableRectF.bottom = thumbDrawDrawableRectF.top + srcHeight
            if (thumbDrawDrawable == null) {
                thumbDrawDrawable =
                    thumbDrawable?.scale(srcWidth.toInt(), srcHeight.toInt(), true)
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
     * 滑块为圆形时，图片绘制路径
     */
    private fun resetCircleThumbDrawablePath() {
        val radius = thumbBackgroundWidth / 2
        val cy = thumbBackgroundRectF.top + radius
        //确定图片位置和大小
        var iconSize: Float = thumbBackgroundWidth - (thumbPadding * 2f)
        thumbDrawDrawableRectF.right = thumbRightX - thumbPadding
        thumbDrawDrawableRectF.left = thumbDrawDrawableRectF.right - iconSize
        (iconSize / 2).let {
            thumbDrawDrawableRectF.top = cy - it
            thumbDrawDrawableRectF.bottom = cy + it
            if (thumbDrawDrawable == null) {
                thumbDrawDrawable =
                    thumbDrawable?.scale(iconSize.toInt(), iconSize.toInt(), true)
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
        onDrawUnlockLockText(canvas)
    }

    /**
     * 绘制滑块
     * @param canvas Canvas
     */
    override fun onDrawForeground(canvas: Canvas?) {
        super.onDrawForeground(canvas)
        onDrawThumbBackground(canvas)
        onDrawThumbContent(canvas)
    }


    /**
     * 解锁提示文字
     * @param canvas Canvas?
     */
    protected open fun onDrawUnlockLockText(canvas: Canvas?) {
        onShowUnLockText()
        if (shineEffect && !shineAnimator.isRunning) {
            unlockLockTextPaint.shader = gradient
            shineAnimator.start()
        }
        canvas?.drawText(
            unlockLockText.orEmpty(),
            trackRectF.centerX() - unlockLockTextWidth / 2,
            unlockLockTextDrawY,
            unlockLockTextPaint
        )
    }

    /**
     * 绘制滑块背景
     * @param canvas Canvas?
     */
    protected open fun onDrawThumbBackground(canvas: Canvas?) {
        canvas?.drawPath(thumbBackgroundPath, thumbBackgroundPaint)
    }

    /**
     * 滑块中间内容
     * @param canvas Canvas?
     */
    protected open fun onDrawThumbContent(canvas: Canvas?) {
        if (thumbType == ThumbType.TEXT) {
            resetThumbTextPosition()
            canvas?.drawText(
                thumbContentText,
                thumbContentTextDrawX,
                thumbContentTextDrawY,
                thumbContentTextPaint
            )
        } else {
            canvas?.drawBitmap(
                thumbDrawDrawable!!,
                null,
                thumbDrawDrawableRectF,
                thumbContentDrawablePaint
            )
        }
    }

    /**
     * 计算滑块文字绘制X轴位置
     */
    protected open fun resetThumbTextPosition() {
        thumbContentTextDrawX = thumbBackgroundRectF.centerX() - thumbContentTextWidth / 2
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
                slidingStarY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (isInScrollRange(slidingStarX, slidingStarY)) {
                    setThumbMoveEffect(event)
                    resetThumbPath()
                    setSlideUnlockProgress()
                    postInvalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isInScrollRange(slidingStarX, slidingStarY)) {
                    setSlideUnlockResult()
                    slidingStarX = 0f
                    slidingStarY = 0f
                }

            }
        }
        return true
    }

    /**
     * 设置解锁进度
     */
    protected open fun setSlideUnlockProgress() {
        unlockCallback?.run {
            val distance = thumbRightBorder - thumbStartRightX
            val distanceProgress = thumbRightX - thumbStartRightX
            onSlideUnlockProgress(this@SlideUnlockView, distanceProgress / distance)
        }
    }

    /**
     * 设置滑动解锁结果
     */
    protected open fun setSlideUnlockResult() {
        if (thumbBackgroundRectF.right >= thumbRightBorder) {
            unlockCallback?.onSlideUnlockComplete(this)
        } else {
            slidingDistance = thumbBackgroundRectF.left - thumbLeftBorder
            springAnimator.start()
        }
    }

    /**
     * 解锁状态回调
     * @param callback OnSlideUnlockCallback
     */
    fun setSlideUnlockCallback(callback: OnSlideUnlockCallback) {
        this.unlockCallback = callback
    }


    /**
     * 设置滑块移动位置效果
     * @param event MotionEvent
     */
    protected open fun setThumbMoveEffect(event: MotionEvent) {
        slidingDistance = event.x - slidingStarX
        thumbLeftX += slidingDistance
        if (thumbLeftX > thumbRightBorder - thumbBackgroundWidth) {
            thumbLeftX = thumbRightBorder - thumbBackgroundWidth
        }
        if (thumbLeftX < thumbLeftBorder) {
            thumbLeftX = thumbLeftBorder
        }
        slidingStarX = event.x
        thumbRightX = thumbLeftX + thumbBackgroundWidth
    }

    /**
     * 判断是否显示解锁提示文字
     */
    protected open fun onShowUnLockText() {
        if (thumbLeftX > thumbLeftBorder) {
            unlockLockTextPaint.alpha = 0
        } else {
            unlockLockTextPaint.alpha = 255
        }
    }


    /**
     * 处理滑动时间冲突
     * @param event MotionEvent
     * @return Boolean
     */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                firstX = x
                firstY = y
            }
            MotionEvent.ACTION_MOVE -> {
                distanceX = abs(x - firstX)
                distanceY = abs(y - firstY)
                if (distanceX > touchSlop && distanceX > distanceY) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
            }
            MotionEvent.ACTION_UP -> {
                firstX = 0
                firstY = 0
            }
        }
        return super.dispatchTouchEvent(event)
    }


    /**
     * 手指是否在滑块按钮上
     * @return Boolean
     */
    private fun isInScrollRange(x: Float, y: Float): Boolean {
        return thumbBackgroundRectF.contains(x, y)
    }


}
