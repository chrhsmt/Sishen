package com.chrhsmt.sisheng.font

import android.app.Activity
import android.graphics.Typeface
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.chrhsmt.sisheng.R
import com.chrhsmt.sisheng.Settings

/**
 * Created by hkimura on 2017/10/31.
 */
object FontUtils {

    fun changeButtonFont(activity: Activity, id: Int) {
        if (Settings.USE_MOE_FONT == false) {
            return
        }

        val button = activity.findViewById<Button>(id) ?: return

        val typeface = Typeface.createFromAsset(
                activity.assets,
                "fonts/SentyCreamPuff.ttf")
        button.setTypeface(typeface)

        if (Settings.EMULATOR_MODE) {
            button.textSize = button.textSize.times(1.5).toFloat()
        }
    }
}