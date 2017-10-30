package com.chrhsmt.sisheng.point

import com.chrhsmt.sisheng.Settings

/**
 * O(N * log(N))のポイント計算クラス.
 * Created by chihiro on 2017/10/30.
 */
class NMultiplyLogarithmPointCalculator : SimplePointCalculator() {

    override fun getScore(nomalizedDistance: Double): Int {
        val base = (Settings.baseLogarithmForPoint).toDouble()
        val n = Math.max(nomalizedDistance - 10, 1.0).toDouble()
        // loge(x) / loge(a) = loga(x)
        val log = Math.log(n) / Math.log(base)
        return Math.max(100 - (n * log), 0.0).toInt()
    }
}