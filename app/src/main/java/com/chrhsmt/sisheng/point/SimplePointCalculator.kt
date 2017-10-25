package com.chrhsmt.sisheng.point

import de.qaware.chronix.distance.DistanceFunctionEnum
import de.qaware.chronix.distance.DistanceFunctionFactory
import de.qaware.chronix.dtw.FastDTW
import de.qaware.chronix.dtw.TimeWarpInfo
import de.qaware.chronix.timeseries.MultivariateTimeSeries

/**
 * Created by chihiro on 2017/10/10.
 */
class SimplePointCalculator : PointCalculator() {

    override fun calc(frequencies: MutableList<Float>, testFrequencies: MutableList<Float>): Point {

        var analyzedFreqList: MutableList<Float> = this.copy(frequencies)
        this.adjustFrequencies(analyzedFreqList)

        analyzedFreqList = this.removeLastSilence(analyzedFreqList)
        this.minimizeSilence(analyzedFreqList)
        val exampleFrequencies = this.removeLastSilence(testFrequencies)

        val info: TimeWarpInfo = this.calcDistance(analyzedFreqList, exampleFrequencies)

        val score = this.getScore(info.normalizedDistance)
        return Point(
                score,
                info.distance,
                info.normalizedDistance,
                this.getBase(info))

//        var items = this.frequencies.mapIndexed { index, fl -> TimeSeriesItem(index.toDouble(), TimeSeriesPoint(kotlin.DoubleArray(1){ fl.toDouble() })) }
//        val ts0 = TimeSeriesBase(items)
//
//        items = testFrequencies.mapIndexed { index, fl -> TimeSeriesItem(index.toDouble(), TimeSeriesPoint(kotlin.DoubleArray(1){ fl.toDouble() })) }
//        val ts1 = TimeSeriesBase(items)
//        return FastDTW.compare(ts0, ts1, Distances.EUCLIDEAN_DISTANCE)
//    }
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