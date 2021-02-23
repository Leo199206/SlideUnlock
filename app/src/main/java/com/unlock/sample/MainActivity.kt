package com.unlock.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.slide.unlock.OnSlideUnlockCallback
import kotlinx.android.synthetic.main.activity_main.*

/**
 * <pre>
 *   @author : leo
 *   @time   : 2021/02/22
 *   @desc   : 滑动解锁Demo
 * </pre>
 */
class MainActivity : AppCompatActivity(), OnSlideUnlockCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        slide_circle.setSlideUnlockCallback(this)
        slide_square.setSlideUnlockCallback(this)
        slide_ios_circle.setSlideUnlockCallback(this)
        slide_ios_square.setSlideUnlockCallback(this)
    }

    override fun onSlideUnlock(success: Boolean) {
        val text = if (success) {
            "解锁成功"
        } else {
            "解锁取消"
        }
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}