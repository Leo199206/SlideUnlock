package com.slide.unlock.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * <pre>
 *   @author : leo
 *   @time   : 2021/02/23
 *   @desc   : iOS 滑动解锁效果
 * </pre>
 */
class FlexibleSlideUnlockView : SlideUnlockView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    /**
     * 设置滑块移动效果
     * @param event MotionEvent
     */
    override fun setThumbScrollEffect(event: MotionEvent) {
        super.setThumbScrollEffect(event)
        resetTrackPath()
        if (thumbLeftX > thumbLeftBorder) {
            textPaint.alpha = 0
        } else {
            textPaint.alpha = 255
        }
    }

    /**
     * 背景伸缩效果
     * @param value Float
     */
    override fun setSpringEffect(value: Float) {
        super.setSpringEffect(value)
        resetTrackPath()
        if (value == 0f) {
            textPaint.alpha = 255
        }
    }
}