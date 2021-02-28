package com.slide.unlock

/**
 * <pre>
 *   @author : leo
 *   @time   : 2021/02/28
 *   @desc   : 滑块类型
 * </pre>
 * @see TEXT 文本
 * @see DRAWABLE 图片
 */
enum class ThumbType(var value: Int) {
    TEXT(0),
    DRAWABLE(1);

    companion object {
        /**
         * value to ThumbType
         * @param value Int
         */
        @JvmStatic
        fun parse(value: Int): ThumbType {
            return values().singleOrNull {
                it.value == value
            } ?: DRAWABLE
        }
    }
}