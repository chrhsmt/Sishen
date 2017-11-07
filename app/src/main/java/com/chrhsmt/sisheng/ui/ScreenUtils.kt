package com.chrhsmt.sisheng.ui

import android.app.Activity
import android.view.View
import android.view.Window
import com.chrhsmt.sisheng.R
import com.chrhsmt.sisheng.Settings

/**
 * Created by hkimu on 2017/11/03.
 */
object ScreenUtils {

    fun setFullScreen(window: Window) {
        // フルスクリーンにする
        val decor = window.decorView
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    fun setScreenBackground(activity: Activity) {
        activity.findViewById<View>(R.id.screenBackground)?.setBackgroundResource(
            if (Settings.PRACTICE_STAFF_SCRIPT)
                R.drawable.first_screen_bg_test
            else
                R.drawable.first_screen_bg
        )
    }
}