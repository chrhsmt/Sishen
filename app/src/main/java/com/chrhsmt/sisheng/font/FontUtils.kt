package com.chrhsmt.sisheng.font

import android.app.Activity
import android.graphics.Typeface
import android.widget.TextView
import com.chrhsmt.sisheng.Settings

/**
 * Created by hkimura on 2017/10/31.
 */
object FontUtils {
    fun changeFont(activity: Activity, view: TextView?, multiple: Float) {
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

        if (multiple != 1.0f) {
            view.textSize = view.textSize.times(multiple).toFloat()
        }
    }

    fun changeFont(activity: Activity, view: TextView?) {
        changeFont(activity, view, 1.0f)
    }

    fun changeFont(activity: Activity, viewId: Int) {
        if (Settings.USE_MOE_FONT == false) {
            return
        }
        val view: TextView? = activity.findViewById<TextView>(viewId)
        changeFont(activity, view)
    }
}