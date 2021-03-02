package com.slide.unlock.view

import android.content.Context
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
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
        Log.d("setThumbMoveEffect", "thumbLeftX:${event}")
        slidingDistance = event.x - slidingStarX
        thumbRightX += slidingDistance
        if (thumbRightX > thumbRightBorder) {
            thumbRightX = thumbRightBorder
        }
        if (thumbRightX < thumbLeftX + thumbBackgroundWidth) {
            thumbRightX = thumbLeftX + thumbBackgroundWidth
        }
        Log.d("setThumbMoveEffect", "thumbLeftX:${thumbLeftX + thumbBackgroundWidth}")
        Log.d("setThumbMoveEffect", "thumbRightX:${thumbRightX}")
        Log.d("setThumbMoveEffect", "thumbRightX:${thumbRightX}")
        slidingStarX = event.x
        resetThumbPath()
    }

    /**
     * 回弹效果更新
     * @param value Float
     */
    override fun setSpringEffect(value: Float) {
        thumbRightX = thumbLeftX + thumbBackgroundWidth + (slidingDistance * value)
        resetThumbPath()
    }

    /**
     * 设置滑动解锁结果
     */
    override fun setSlideUnlockResult() {
        if (thumbBackgroundRectF.right >= thumbRightBorder) {
            unlockCallback?.onSlideUnlock(true)
        } else {
            slidingDistance = thumbBackgroundRectF.right - thumbLeftX - thumbBackgroundWidth
            springAnimator.start()
        }
    }

    /**
     * 圆形滑块背景绘制路径
     */
    override fun resetCircleThumbBackgroundPath() {
        //确定滑块绘制坐标和范围
        thumbBackgroundRectF.right = thumbRightX
        if (thumbRightX <= thumbLeftX + thumbBackgroundWidth) {
            val radius = thumbBackgroundWidth / 2
            val cx = thumbBackgroundRectF.left + radius
            val cy = thumbBackgroundRectF.top + radius
            thumbBackgroundPath.addCircle(cx, cy, radius, Path.Direction.CW)
        } else {
            thumbBackgroundRoundCorner = trackRoundCorner
            thumbBackgroundPath.addRoundRect(
                thumbBackgroundRectF,
                thumbBackgroundRoundCorner,
                thumbBackgroundRoundCorner,
                Path.Direction.CW
            )
        }
    }
}