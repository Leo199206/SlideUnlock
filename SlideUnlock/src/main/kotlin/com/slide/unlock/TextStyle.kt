package com.slide.unlock

import android.graphics.Typeface

/**
 * <pre>
 *   @author : leo
 *   @time   : 2021/02/28
 *   @desc   : 字体风格
 * </pre>
 */
enum class TextStyle(var value: Typeface) {
    DEFAULT(Typeface.DEFAULT),
    BOLD(Typeface.DEFAULT_BOLD);

    companion object {
        /**
         * value to ThumbShape
         * @param value Int
         */
        @JvmStatic
        fun parse(value: Int): TextStyle {
            return values().singleOrNull {
                it.value.style == value
            } ?: DEFAULT
        }
    }
}