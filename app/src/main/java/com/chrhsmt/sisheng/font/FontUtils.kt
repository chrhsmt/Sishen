package com.chrhsmt.sisheng.font

import android.app.Activity
import android.graphics.Typeface
import android.support.annotation.Nullable
import android.view.View
import android.view.WindowId
import android.widget.Button
import android.widget.TextView
import com.chrhsmt.sisheng.R
import com.chrhsmt.sisheng.Settings

/**
 * Created by hkimura on 2017/10/31.
 */
object FontUtils {
    fun changeFont(activity: Activity, view: TextView?) {
        if (Settings.USE_MOE_FONT == false) {
            return
        }
        if (view == null) {
            return
        }

        val typeface = Typeface.createFromAsset(
                activity.assets,
                "fonts/SentyCreamPuff.ttf")
        view.setTypeface(typeface)

        if (Settings.EMULATOR_MODE) {
            view.textSize = view.textSize.times(1.5).toFloat()
        }
    }
    fun changeFont(activity: Activity, viewId: Int) {
        if (Settings.USE_MOE_FONT == false) {
            return
        }
        val view: TextView? = activity.findViewById<TextView>(viewId)
        changeFont(activity, view)
    }
}