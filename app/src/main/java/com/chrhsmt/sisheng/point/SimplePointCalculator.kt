package com.chrhsmt.sisheng.point

import android.util.Log
import com.chrhsmt.sisheng.Settings
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
        enum class NOISE_RECUDER(val type: Int) {
            V1(0),
            V2(1)
        }
    }

    var calirationType: CALIBRATION_TYPE = CALIBRATION_TYPE.SEX
    var noizeReducer: NOISE_RECUDER = NOISE_RECUDER.V1

    fun setCalibrationType(type: CALIBRATION_TYPE) {
        this.calirationType = type
    }
    fun setNoiseReducer(type: NOISE_RECUDER) {
        this.noizeReducer = type
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

    override fun removeNoises(freqList: MutableList<Float>) {
        if (this.noizeReducer == NOISE_RECUDER.V1) {
            super.removeNoises(freqList)
        } else {
            super.removeNoises(freqList)
            this.simpleMovingAverage(freqList)
        }
    }

    /**
     * だめかも
     */
    fun removeNoisesV2(freqList: MutableList<Float>) {
        val freqSize = freqList.size
        val noizeIndexList: MutableList<Int> = arrayListOf()

        // 反転したListのインスタンスでループ
        freqList.reversed().forEachIndexed { index, fl ->
            // 実Index
            val realIndex = freqSize - 1 - index

            // 前後比較
            val target = freqList.get(realIndex)
            val after = freqList.getOrElse<Float>(realIndex + 1) { 0f }
            val before = freqList.getOrElse<Float>(realIndex - 1) { 0f }
            val rebefore = freqList.getOrElse<Float>(realIndex - 2) { 0f }
            val rerebefore = freqList.getOrElse<Float>(realIndex - 3) { 0f }
            if ((after + before + rebefore + rerebefore) / 3 < target) {
                noizeIndexList.add(realIndex)
            }

            if (realIndex == 0 && noizeIndexList.isNotEmpty()) {
                noizeIndexList.forEach { i ->
                    freqList.removeAt(i)
                }
            }
        }
    }

    fun simpleMovingAverage(freqList: MutableList<Float>) {
        var index = 0
        val ret = freqList.windowed(5, 1, false) { list ->
            val ave = list.filter { fl -> fl > 0 }.average()
            if (ave > 0 && ave * 1.3 < list.first()) {
                return@windowed index++
            } else {
                index++
                return@windowed null
            }
        }
        ret.filterNotNull().reversed().forEach { index ->
            freqList.removeAt(index)
        }
        Log.d("TAG", "a")
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