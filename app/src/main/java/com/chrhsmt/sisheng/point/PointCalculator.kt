package com.chrhsmt.sisheng.point

import android.util.Range
import com.chrhsmt.sisheng.AudioService
import com.chrhsmt.sisheng.Settings
import de.qaware.chronix.distance.DistanceFunctionEnum
import de.qaware.chronix.distance.DistanceFunctionFactory
import de.qaware.chronix.dtw.FastDTW
import de.qaware.chronix.dtw.TimeWarpInfo
import de.qaware.chronix.timeseries.MultivariateTimeSeries
import java.util.*

/**
 * Created by chihiro on 2017/10/10.
 */
abstract class PointCalculator {
    companion object {
        // 性別での周波数差
        val HELZT_DEGREE_OF_SEX_DEFERENCE: Int = 60
        // 例題ファイルの性別タイプ取得用の正規表現
        val EXAMPLE_FILE_SEX_TYPE_REFEX = Regex("^.*_(f|m).wav$")
    }
    abstract fun calc(frequencies: MutableList<Float>, testFrequencies: MutableList<Float>): Point

    /**
     * 音声周波数の男女差を調整する
     */
    fun adjustFrequencies(analyzedFreqList: MutableList<Float>) {
        val fileSexType = this.getExampleFileSexType()
        val selectedSexType = Settings.sex!!.first().toLowerCase().toString()

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
    }

    fun minimizeSilence(freqList: MutableList<Float>) {

        // サンプル1つの時間の長さ(ms)
        val oneSampleTimeMS = (1.0 / Settings.samplingRate!!.toFloat()) * AudioService.BUFFER_SIZE * 1000
        val count:Double = 250!! / oneSampleTimeMS

        var tmpIndex: Int? = null
        var tmpRange: IntRange? = null
        var zeroRangeList: MutableList<IntRange> = ArrayList<IntRange>()
        freqList.forEachIndexed { index, fl ->
            if (fl <= 0) {
                if (tmpIndex == null) {
                    // 最初の0
                    tmpIndex = index
                } else {
                    // 0が続いている
                    tmpRange = (tmpIndex!!..index)
                }
            } else {
                tmpIndex = null
                if (tmpRange != null && tmpRange!!.count().toDouble() >= count) {
                    // 一定時間経過した無音があれば採用
                    zeroRangeList.add(tmpRange!!)
                }
                tmpRange = null
            }
        }

        val filler = listOf(0, 0, 0, 0)
        zeroRangeList.reverse()
        zeroRangeList.forEach { range ->
            range.reversed().forEach { i ->
                // TODO:半分？縮める
                freqList.removeAt(i)
            }
            freqList.addAll(range.first, filler as Collection<Float>)
            range.count()
        }

    }

    /**
     * Removing noises.
     * @freqList List of frequency
     */
    fun removeNoises(freqList: MutableList<Float>) {

        val noizeMaxLength = Settings.freqNoizeCountThreashold
        val freqSize = freqList.size
        val noizeIndexList: MutableList<Int> = arrayListOf()

        // 反転したListのインスタンスでループ
        freqList.reversed().forEachIndexed { index, fl ->
            // 実Index
            val realIndex = freqSize - 1 - index

            if (fl > 0) {
                // ノイズ発見、indexにする
                noizeIndexList.add(realIndex)
            }

            if (noizeIndexList.size > noizeMaxLength) {
                // ノイズ判定サイズを超えたものはノイズとみなさないのでクリアー
                noizeIndexList.clear()
            }

            if (fl <= 0) {
                // 無音
                if (noizeIndexList.size <= noizeMaxLength) {
                    noizeIndexList.forEach { i ->
                        // 除去(後ろから)
                        freqList.removeAt(i)
                    }
                }
                noizeIndexList.clear()
            }

            if (realIndex == 0 && noizeIndexList.isNotEmpty()) {
                noizeIndexList.forEach { i ->
                    freqList.removeAt(i)
                }
            }
        }
    }

    fun calcDistance(analyzedFreqList: List<Float>, exampleFrequencies: List<Float>): TimeWarpInfo {

        // chronix.fastdtw
        val ts0 = MultivariateTimeSeries(1)
        analyzedFreqList.forEachIndexed { index, fl -> ts0.add(index.toLong(), kotlin.DoubleArray(1) { fl.toDouble() }) }
        val ts1 = MultivariateTimeSeries(1)
        exampleFrequencies.forEachIndexed { index, fl -> ts1.add(index.toLong(), kotlin.DoubleArray(1) { fl.toDouble() }) }
        val info: TimeWarpInfo = FastDTW.getWarpInfoBetween(ts0, ts1, 1, DistanceFunctionFactory.getDistanceFunction(DistanceFunctionEnum.EUCLIDEAN))

        return info
    }

    fun removeLastSilence(frequencies: MutableList<Float>): MutableList<Float> {
        val list = frequencies.subList(0, frequencies.indexOfLast { fl -> fl > 0 } + 1)
        return list
    }

    fun copy(list: MutableList<Float>) : MutableList<Float> {
        var result: MutableList<Float> = ArrayList<Float>()
        result.addAll(list)
        return result
    }

    fun getExampleFileSexType() : String {
        val result = EXAMPLE_FILE_SEX_TYPE_REFEX.find(Settings.sampleAudioFileName!!)
        return result!!.groups[1]!!.value
    }

}