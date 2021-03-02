package com.slide.unlock.view

import android.content.Context
import android.graphics.Path
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
        var x = correctThumbX(event)
        slidingDistance = x - slidingStarX
        slidingStarX = x
        thumbRightX += slidingDistance
        if (thumbRightX > thumbRightBorder) {
            thumbRightX = thumbRightBorder
            slidingStarX = thumbRightBorder
        }
        if (thumbRightX < thumbLeftBorder + thumbBackgroundWidth) {
            thumbRightX = thumbLeftBorder + thumbBackgroundWidth
            slidingStarX = thumbRightX
        }

    }


    /**
     * 回弹效果更新
     * @param value Float
     */
    override fun setSpringEffect(value: Float) {
        thumbRightX = thumbLeftX + thumbBackgroundWidth + slidingDistance * value
    }


    /**
     * 圆形滑块背景绘制路径
     */
    override fun resetCircleThumbBackgroundPath() {
        //确定滑块绘制坐标和范围
        if (thumbRightX < thumbLeftBorder + thumbBackgroundWidth) {
            val radius = thumbBackgroundWidth / 2
            val cx = thumbBackgroundRectF.left + radius
            val cy = thumbBackgroundRectF.top + radius
            thumbRightX = thumbLeftBorder + thumbBackgroundWidth
            thumbBackgroundRectF.right = thumbLeftBorder + thumbBackgroundWidth
            thumbBackgroundPath.addCircle(cx, cy, radius, Path.Direction.CW)
        } else {
            thumbBackgroundRectF.right = thumbRightX
            thumbBackgroundRoundCorner = trackRoundCorner
            thumbBackgroundPath.addRoundRect(
                thumbBackgroundRectF,
                thumbBackgroundRoundCorner,
                thumbBackgroundRoundCorner,
                Path.Direction.CW
            )
        }
    }

    /**
     * 计算滑块文字绘制X轴位置
     */
    override fun resetThumbTextPosition() {
        thumbContentTextDrawX =
            thumbBackgroundRectF.right - thumbBackgroundWidth / 2 - thumbContentTextWidth / 2
    }

    /**
     * 判断是否显示解锁提示文字
     */
    override fun onShowUnLockText() {
        if (thumbRightX > thumbLeftX + thumbBackgroundWidth) {
            unlockLockTextPaint.alpha = 0
        } else {
            unlockLockTextPaint.alpha = 255
        }
    }

}
