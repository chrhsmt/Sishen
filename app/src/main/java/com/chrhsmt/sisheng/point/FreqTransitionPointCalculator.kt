package com.chrhsmt.sisheng.point

import de.qaware.chronix.dtw.TimeWarpInfo

/**
 * 一つ前の音のデータと比較して遷移を計算する.
 *
 * Created by chihiro on 2017/10/17.
 */
class FreqTransitionPointCalculator : PointCalculator() {

    override fun calc(frequencies: MutableList<Float>, testFrequencies: MutableList<Float>): Point {
        var analyzedFreqList: MutableList<Float> = this.copy(frequencies)
//        this.adjustFrequencies(analyzedFreqList)

        analyzedFreqList = this.removeLastSilence(analyzedFreqList)
        this.minimizeSilence(analyzedFreqList)
        val exampleFrequencies = this.removeLastSilence(testFrequencies)

        val transitionList: List<Float> = this.calcTransitions(analyzedFreqList)
        val testTransitionList: List<Float> = this.calcTransitions(testFrequencies)

        val info = this.calcDistance(transitionList, testTransitionList)

        val score = this.getScore(info.normalizedDistance)
        return Point(
                score,
                info.distance,
                info.normalizedDistance,
                this.getBase(info),
                analyzedFreqList)
    }

    private fun calcTransitions(frequencies: MutableList<Float>): List<Float> {

        val retList: MutableList<Float> = ArrayList<Float>()

        var before:Float? = null
        frequencies.forEach { fl: Float ->
            if ( before != null ) {
                retList.add(before!! - fl)
            }
            before = fl
        }

        return retList
    }

    private fun getScore(nomalizedDistance: Double): Int {
        return 100 - Math.max(nomalizedDistance.toInt() - 10, 0)
    }

    private fun getBase(info: TimeWarpInfo) : Int {
        val field = TimeWarpInfo::class.java.getDeclaredField("base")
        field.isAccessible = true
        return field.getInt(info)
    }

}