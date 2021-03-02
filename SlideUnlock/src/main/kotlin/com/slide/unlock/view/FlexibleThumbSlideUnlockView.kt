package com.slide.unlock.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * <pre>
 *   @author : leo
 *   @time   : 2021/03/02
 *   @desc   : iOS 滑动解锁，滑块收缩效果
 * </pre>
 */
class FlexibleThumbSlideUnlockView : SlideUnlockView {
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
        slidingDistance = event.x - slidingStarX
        thumbLeftX += slidingDistance
        if (thumbLeftX > thumbRightBorder - thumbBackgroundWidth) {
            thumbLeftX = thumbRightBorder - thumbBackgroundWidth
        }
        if (thumbLeftX < thumbLeftBorder) {
            thumbLeftX = thumbLeftBorder
        }
        slidingStarX = event.x
        resetThumbPath()
    }
}