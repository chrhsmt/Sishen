package com.chrhsmt.sisheng.point

/**
 * Created by chihiro on 2017/10/10.
 */
interface PointCalculator {
    companion object {
        // 性別での周波数差
        val HELZT_DEGREE_OF_SEX_DEFERENCE: Int = 60
    }
    fun calc(frequencies: MutableList<Float>, testFrequencies: MutableList<Float>): Point
}