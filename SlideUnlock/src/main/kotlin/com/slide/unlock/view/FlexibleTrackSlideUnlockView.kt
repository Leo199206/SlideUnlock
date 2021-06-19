package com.slide.unlock.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * <pre>
 *   @author : leo
 *   @time   : 2021/02/23
 *   @desc   : iOS 滑动解锁，背景收缩效果
 * </pre>
 */
class FlexibleTrackSlideUnlockView : SlideUnlockView {
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
    override fun setThumbMoveEffect(event: MotionEvent) {
        super.setThumbMoveEffect(event)
        //滑块移动时，背景长度跟随变化
        setTrackPath()
    }

    /**
     * 背景伸缩效果
     * @param value Float
     */
    override fun setSpringEffect(value: Float) {
        super.setSpringEffect(value)
        //滑块松手回弹时，背景长度跟随变化
        setTrackPath()
        if (value == 0f) {
            unlockLockTextPaint.alpha = 255
        }
    }
}