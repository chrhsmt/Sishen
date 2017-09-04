package com.chrhsmt.sisheng

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.chrhsmt.sisheng.ui.Chart
import org.jtransforms.fft.DoubleFFT_1D

/**
 * Created by chihiro on 2017/08/22.
 */
class AudioService {

    companion object {
        val SAMPLING_RATE: Int = 8000 // 44100 // 通常のFFTで解像度を上げるためにはsamplingRateを下げる
    }

    private val TAG: String = "AudioService"

    private var audioRec: AudioRecord? = null
    private val bufSize: Int
    private val CHUNK_SIZE = 256
    private val dBBaseline = Math.pow(2.0, 15.0) * CHUNK_SIZE * Math.sqrt(2.0)
    private val fft: DoubleFFT_1D = DoubleFFT_1D(CHUNK_SIZE.toLong())
    private val WINDOW: DoubleArray
    private val chart: Chart
    private val cost: Cost = AudioService.Cost()

    constructor(chart: Chart) {
        this.chart = chart
        this.WINDOW = DoubleArray(CHUNK_SIZE)
        // hamming window
        for (i in 0..CHUNK_SIZE - 1) {
            this.WINDOW[i] = 0.54 - 0.46 * Math.cos(2.0 * Math.PI * i.toDouble() / (CHUNK_SIZE - 1))
        }

        // バッファサイズの計算
        var tempSize = AudioRecord.getMinBufferSize(
                SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT)
        if (CHUNK_SIZE > tempSize) {
            tempSize = CHUNK_SIZE
        }
        this.bufSize = tempSize
    }

    fun startAudioRecord() {

        // AudioRecordの作成
        this.audioRec = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                this.bufSize)

        this.audioRec!!.setRecordPositionUpdateListener(object : AudioRecord.OnRecordPositionUpdateListener {
            override fun onMarkerReached(audioRecord: AudioRecord?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onPeriodicNotification(audioRecord: AudioRecord?) {
                val audioData: ShortArray = kotlin.ShortArray(CHUNK_SIZE)
                val size = audioRecord!!.read(audioData, 0, CHUNK_SIZE)
                Log.d(TAG, size.toString())
//                var fftData: DoubleArray = audioData.map { audio -> audio.toDouble() }.toDoubleArray()
                var fftData: DoubleArray = window(audioData)
                fft.realForward(fftData)

                // 簡易的に最大値を探す
                val dbfs = DoubleArray(CHUNK_SIZE / 2)
                var max_spectol = 0.0
                var max_spectol_i = 0
                var max_db = -55.0 // TODO:人の声のデシベルしきい値を考える
                var max_i = 0
                var i = 0
                while (i < CHUNK_SIZE / 2) {
                    if (max_spectol < fftData[i]) {
                        max_spectol = fftData[i]
                        max_spectol_i = i / 2
                    }
                    if (max_spectol < fftData[i + 1]) {
                        max_spectol = fftData[i + 1]
                        max_spectol_i = (i + 1) / 2
                    }
                    dbfs[i / 2] = 20 * Math.log10(Math.sqrt(Math.pow(fftData[i], 2.0) + Math.pow(fftData[i + 1], 2.0)).toInt().toDouble() / dBBaseline)
                    if (max_db < dbfs[i / 2]) {
                        max_db = dbfs[i / 2]
                        max_i = i / 2
                    }
                    i += 2

                }
                val freq: Float = (SAMPLING_RATE / CHUNK_SIZE.toFloat()) * max_i * 2
//                val freq: Float? = cost.put((SAMPLING_RATE / CHUNK_SIZE.toFloat()) * max_i * 2)
//                if (freq != null) {
//                    chart.addEntry(freq)
//                }
                if (freq > 0) {
                    chart.addEntry(freq)
                }
                Log.d("db", "Hz: " + (SAMPLING_RATE / CHUNK_SIZE.toDouble()) * max_i * 2 + ", maxdb :" + max_db + "max_i:" + max_i + "maxspi:" + max_spectol_i);
            }
        })
        this.audioRec!!.setPositionNotificationPeriod(CHUNK_SIZE)
        this.audioRec!!.startRecording()
    }

    fun stop() {
        this.audioRec!!.stop()
    }

    private fun window(audioDatas: ShortArray): DoubleArray {
        val retData: DoubleArray = kotlin.DoubleArray(this.WINDOW.size)
        for (i in 0..this.WINDOW.size - 1) {
            val audio = audioDatas[i].toDouble()
            val window = this.WINDOW[i]
            retData[i] = (audio * window).toDouble()
        }
        return retData
    }

    /**
     * 適当なコスト計算クラス
     */
    class Cost {

        private val LENGTH: Int = 3
        private val map: MutableMap<Float, Int> = mutableMapOf()

        fun put(freq: Float) : Float? {
            if (this.map[freq] == null) {
                this.map.put(freq, 1)
            } else {
                this.map.put(freq, this.map[freq]!!.plus(1))
            }

            var result: Float? = null
            this.map.forEach { entry ->
                if (entry.value >= LENGTH) {
                    result = entry.key
                }
            }
            if (result != null) {
                this.map.clear()
            }
            return result
        }
    }
}