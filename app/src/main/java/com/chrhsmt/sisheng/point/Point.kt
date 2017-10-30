package com.chrhsmt.sisheng.point

import com.chrhsmt.sisheng.Settings

/**
 * Created by chihiro on 2017/10/10.
 */
data class Point(
        val score: Int,
        val distance: Double,
        val normalizedDistance: Double,
        val base: Int) {

    fun success() : Boolean {
        return this.score >= Settings.pointSuccessThreshold
    }

}