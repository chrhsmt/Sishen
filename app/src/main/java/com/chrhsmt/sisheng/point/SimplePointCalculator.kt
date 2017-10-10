package com.chrhsmt.sisheng.point

import com.chrhsmt.sisheng.AudioService
import com.chrhsmt.sisheng.Settings
import de.qaware.chronix.distance.DistanceFunctionEnum
import de.qaware.chronix.distance.DistanceFunctionFactory
import de.qaware.chronix.dtw.FastDTW
import de.qaware.chronix.dtw.TimeWarpInfo
import de.qaware.chronix.timeseries.MultivariateTimeSeries

/**
 * Created by chihiro on 2017/10/10.
 */
class SimplePointCalculator : PointCalculator {

    override fun calc(frequencies: MutableList<Float>, testFrequencies: MutableList<Float>): Point {

        val fileSexType = this.getExampleFileSexType()
        val selectedSexType = Settings.sex!!.first().toLowerCase().toString()
        var analyzedFreqList: MutableList<Float> = this.copy(frequencies)

        if (!selectedSexType.equals(fileSexType)) {
            if (selectedSexType.equals("f")) {
                // female
                analyzedFreqList.forEachIndexed { index, fl ->
                    if (fl > 0) {
                        analyzedFreqList[index] = fl - PointCalculator.HELZT_DEGREE_OF_SEX_DEFERENCE
                        if (analyzedFreqList[index] < 0) {
                            analyzedFreqList[index] = 0F
                        }
                    }
                }
            } else {
                // male
                analyzedFreqList.forEachIndexed { index, fl ->
                    if (fl > 0) {
                        analyzedFreqList[index] = fl + PointCalculator.HELZT_DEGREE_OF_SEX_DEFERENCE
                    }
                }
            }
        }

        analyzedFreqList = this.removeLastSilence(analyzedFreqList)
        val exampleFrequencies = this.removeLastSilence(testFrequencies)

        // chronix.fastdtw
        val ts0 = MultivariateTimeSeries(1)
        analyzedFreqList.forEachIndexed { index, fl -> ts0.add(index.toLong(), kotlin.DoubleArray(1) { fl.toDouble() }) }
        val ts1 = MultivariateTimeSeries(1)
        exampleFrequencies.forEachIndexed { index, fl -> ts1.add(index.toLong(), kotlin.DoubleArray(1) { fl.toDouble() }) }
        val info: TimeWarpInfo = FastDTW.getWarpInfoBetween(ts0, ts1, 1, DistanceFunctionFactory.getDistanceFunction(DistanceFunctionEnum.EUCLIDEAN))

        return Point(0, info.distance, info.normalizedDistance, this.getBase(info))

//        var items = this.frequencies.mapIndexed { index, fl -> TimeSeriesItem(index.toDouble(), TimeSeriesPoint(kotlin.DoubleArray(1){ fl.toDouble() })) }
//        val ts0 = TimeSeriesBase(items)
//
//        items = testFrequencies.mapIndexed { index, fl -> TimeSeriesItem(index.toDouble(), TimeSeriesPoint(kotlin.DoubleArray(1){ fl.toDouble() })) }
//        val ts1 = TimeSeriesBase(items)
//        return FastDTW.compare(ts0, ts1, Distances.EUCLIDEAN_DISTANCE)
//    }
    }

    private fun getBase(info: TimeWarpInfo) : Int {
        val field = TimeWarpInfo::class.java.getDeclaredField("base")
        field.isAccessible = true
        return field.getInt(info)
    }

    private fun removeLastSilence(frequencies: MutableList<Float>): MutableList<Float> {
        val list = frequencies.subList(0, frequencies.indexOfLast { fl -> fl > 0 } + 1)
        return list
    }

    private fun copy(list: MutableList<Float>) : MutableList<Float> {
        var result: MutableList<Float> = ArrayList<Float>()
        result.addAll(list)
        return result
    }

    private fun getExampleFileSexType() : String {
        val regex = Regex("^.*_(f|m).wav$")
        val result = regex.find(Settings.sampleAudioFileName!!)
        return result!!.groups[1]!!.value
    }
}