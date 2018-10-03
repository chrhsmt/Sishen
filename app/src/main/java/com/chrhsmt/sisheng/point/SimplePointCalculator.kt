package com.chrhsmt.sisheng.point

import de.qaware.chronix.distance.DistanceFunctionEnum
import de.qaware.chronix.distance.DistanceFunctionFactory
import de.qaware.chronix.dtw.FastDTW
import de.qaware.chronix.dtw.TimeWarpInfo
import de.qaware.chronix.timeseries.MultivariateTimeSeries

/**
 * Created by chihiro on 2017/10/10.
 */
open class SimplePointCalculator : PointCalculator() {

    companion object {
        enum class CALIBRATION_TYPE(val type: Int) {
            SEX(0),
            FREQ(1)
        }
    }

    var calirationType: CALIBRATION_TYPE = CALIBRATION_TYPE.SEX

    fun setCalibrationType(type: CALIBRATION_TYPE) {
        this.calirationType = type
    }

    override fun calc(frequencies: MutableList<Float>, testFrequencies: MutableList<Float>): Point {

        var analyzedFreqList: MutableList<Float> = this.copy(frequencies)

        // ノイズ除去
        this.removeNoises(analyzedFreqList)

        // 無音除去
        analyzedFreqList = this.removeLastSilence(analyzedFreqList)
        this.minimizeSilence(analyzedFreqList)

        if (this.calirationType == CALIBRATION_TYPE.SEX) {
            // 調整(男女差)
            this.adjustFrequencies(analyzedFreqList)
        } else {
            // 調整(周波数)
            this.calibrateFrequencies(analyzedFreqList, testFrequencies)

        }

        val exampleFrequencies = this.removeLastSilence(testFrequencies)

        val info: TimeWarpInfo = this.calcDistance(analyzedFreqList, exampleFrequencies)

        val score = this.getScore(info.normalizedDistance)
        return Point(
                score,
                info.distance,
                info.normalizedDistance,
                this.getBase(info),
                analyzedFreqList)

//        var items = this.frequencies.mapIndexed { index, fl -> TimeSeriesItem(index.toDouble(), TimeSeriesPoint(kotlin.DoubleArray(1){ fl.toDouble() })) }
//        val ts0 = TimeSeriesBase(items)
//
//        items = testFrequencies.mapIndexed { index, fl -> TimeSeriesItem(index.toDouble(), TimeSeriesPoint(kotlin.DoubleArray(1){ fl.toDouble() })) }
//        val ts1 = TimeSeriesBase(items)
//        return FastDTW.compare(ts0, ts1, Distances.EUCLIDEAN_DISTANCE)
//    }
    }

    open fun getScore(nomalizedDistance: Double): Int {
        return Math.max(100 - Math.max(nomalizedDistance.toInt() - 10, 0), 0)
    }

    private fun getBase(info: TimeWarpInfo) : Int {
        val field = TimeWarpInfo::class.java.getDeclaredField("base")
        field.isAccessible = true
        return field.getInt(info)
    }
}