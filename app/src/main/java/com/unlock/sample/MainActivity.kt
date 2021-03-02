package com.unlock.sample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.slide.unlock.OnSlideUnlockCallback
import com.slide.unlock.view.SlideUnlockView
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
        slide_style1.setSlideUnlockCallback(this)
        slide_style2.setSlideUnlockCallback(this)
        slide_style3.setSlideUnlockCallback(this)
        slide_style4.setSlideUnlockCallback(this)
        slide_style5.setSlideUnlockCallback(this)
        slide_style6.setSlideUnlockCallback(this)
        slide_style7.setSlideUnlockCallback(this)
        slide_style8.setSlideUnlockCallback(this)
        slide_style9.setSlideUnlockCallback(this)
        slide_style10.setSlideUnlockCallback(this)
        slide_style11.setSlideUnlockCallback(this)
        slide_style12.setSlideUnlockCallback(this)
    }

    override fun onSlideUnlockComplete(view: SlideUnlockView) {
        Toast.makeText(this, "成功", Toast.LENGTH_SHORT).show()
    }

    override fun onSlideUnlockProgress(view: SlideUnlockView, progress: Float) {
        tv_progress.text = "解锁进度为:${(progress * 100).toInt()}%"
    }
}