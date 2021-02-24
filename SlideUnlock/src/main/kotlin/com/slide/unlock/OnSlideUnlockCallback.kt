package com.slide.unlock

/**
 * <pre>
 *   @author : leo
 *   @time   : 2021/02/22
 *   @desc   : 滑动解锁状态回调
 * </pre>
 */
interface OnSlideUnlockCallback {

    /**
     * Slide to unlock callback
     * true means unlock success, false means unlock failure
     * @param success Boolean
     */
    fun onSlideUnlock(success: Boolean)
}