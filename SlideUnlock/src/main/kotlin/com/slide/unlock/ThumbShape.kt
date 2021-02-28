package com.slide.unlock

/**
 * <pre>
 *   @author : leo
 *   @time   : 2021/02/21
 *   @desc   : 滑块形状
 * </pre>
 * @see CIRCLE 圆形
 * @see SQUARE 方形
 */
enum class ThumbShape(var value: Int) {
    CIRCLE(0),
    SQUARE(1);

    companion object {
        /**
         * value to ThumbShape
         * @param value Int
         */
        @JvmStatic
        fun parse(value: Int): ThumbShape {
            return values().singleOrNull {
                it.value == value
            } ?: CIRCLE
        }
    }

}