package com.chrhsmt.sisheng.ui

import android.app.Activity
import android.view.View
import android.view.Window

/**
 * Created by hkimu on 2017/11/03.
 */
object ScreenUtils {

    fun setFullScreen(window: Window) {
        // フルスクリーンにする
        val decor = window.decorView
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

}