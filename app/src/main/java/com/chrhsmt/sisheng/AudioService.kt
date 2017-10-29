package com.chrhsmt.sisheng

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.media.*
import android.util.Log
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchDetectionResult
import be.tarsos.dsp.pitch.PitchProcessor
import com.chrhsmt.sisheng.ui.Chart
import android.media.AudioManager
import be.tarsos.dsp.io.android.AndroidAudioPlayer
import com.chrhsmt.sisheng.point.Point
import com.chrhsmt.sisheng.point.PointCalculator
import com.chrhsmt.sisheng.point.SimplePointCalculator
import com.github.mikephil.charting.utils.ColorTemplate
import de.qaware.chronix.distance.DistanceFunctionEnum
import de.qaware.chronix.distance.DistanceFunctionFactory
import de.qaware.chronix.dtw.FastDTW
import de.qaware.chronix.dtw.TimeWarpInfo
import de.qaware.chronix.timeseries.MultivariateTimeSeries
import kotlinx.android.synthetic.main.content_main.*
import kotlin.reflect.KClass


/**
 * Created by chihiro on 2017/08/22.
 */
class AudioService {

    companion object {
//        val SAMPLING_RATE: Int = 22050 // 44100
        val AUDIO_FILE_SAMPLING_RATE: Int = 44100
        // 録音時に指定秒数の空白時間後に録音停止
        val STOP_RECORDING_AFTER_SECOND: Int = 2
        val BUFFER_SIZE: Int = 1024
        val MAX_FREQ_THRESHOLD = 500f
    }

    private val TAG: String = "AudioService"

    private val activity: MainActivity
    private val chart: Chart
    private var audioDispatcher: AudioDispatcher? = null
    private var analyzeThread: Thread? = null
    private var isRunning: Boolean = false

    private var frequencies: MutableList<Float> = ArrayList<Float>()
    private var testFrequencies: MutableList<Float> = ArrayList<Float>()

    constructor(chart: Chart, activity: MainActivity) {
        this.activity = activity
        this.chart = chart
        // Setting ffmpeg
        AndroidFFMPEGLocator(this.activity)
    }

    fun startAudioRecord() {
        // マイクロフォンバッファサイズの計算
        val microphoneBufferSize = AudioRecord.getMinBufferSize(
                Settings.samplingRate!!,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT) / 2
        this.startRecord(
                AudioDispatcherFactory.fromDefaultMicrophone(Settings.samplingRate!!, microphoneBufferSize, 0),
                targetList = this.frequencies,
                labelName = "Microphone",
                color = Color.rgb(10, 240, 10)
        )
    }

    @SuppressLint("WrongConstant")
    fun testPlay(fileName: String) {

        Thread(Runnable {
            // ファイル移動
            var dataName = fileName
            if (fileName.contains("/")) {
                dataName = fileName.replace("/", "_")
            }
            val path = String.format("/data/data/%s/files/%s", this.activity.packageName, dataName)
            val input = this.activity.assets.open(fileName)
            val output = this.activity.openFileOutput(dataName, Context.MODE_ENABLE_WRITE_AHEAD_LOGGING)
            val DEFAULT_BUFFER_SIZE = 1024 * 4

            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var n = 0
            while (true) {
                n = input.read(buffer)
                if (n == -1) break
                output.write(buffer, 0, n)
            }
            output.close()
            input.close()

            this.startRecord(
                    AudioDispatcherFactory.fromPipe(
                            path,
                            AUDIO_FILE_SAMPLING_RATE,
                            BUFFER_SIZE,
                            0
                    ),
                    false,
                    playback = true,
                    samplingRate = AUDIO_FILE_SAMPLING_RATE,
                    targetList = this.testFrequencies,
                    labelName = "SampleAudio"
            )
        }).start()

    }

    @SuppressLint("WrongConstant")
    fun attemptPlay(fileName: String) {

        AndroidFFMPEGLocator(this.activity)

        Thread(Runnable {
            // ファイル移動
            val dataName = fileName.replace("/", "_")
            val path = String.format("/data/data/%s/files/%s", this.activity.packageName, dataName)
            val input = this.activity.assets.open(fileName)
            val output = this.activity.openFileOutput(dataName, Context.MODE_ENABLE_WRITE_AHEAD_LOGGING)
            val DEFAULT_BUFFER_SIZE = 1024 * 4

            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var n = 0
            while (true) {
                n = input.read(buffer)
                if (n == -1) break
                output.write(buffer, 0, n)
            }
            output.close()
            input.close()

            this.startRecord(
                    AudioDispatcherFactory.fromPipe(
                            path,
                            AUDIO_FILE_SAMPLING_RATE,
                            BUFFER_SIZE,
                            0
                    ),
                    false,
                    playback = true,
                    samplingRate = AUDIO_FILE_SAMPLING_RATE,
                    targetList = this.frequencies,
                    labelName = "RecordedSampleAudio",
                    color = Color.rgb(255, 10, 10)
            )
        }).start()

    }

    fun stop() {
        this.stopRecord()
    }

    fun analyze(klassName: String = SimplePointCalculator::class.qualifiedName!!) : Point {
        val calculator: PointCalculator = Class.forName(klassName).newInstance() as PointCalculator
        return calculator.calc(this.frequencies, this.testFrequencies)
    }

    fun clear() {
        this.frequencies.clear()
        this.testFrequencies.clear()
    }

    private fun startRecord(dispatcher: AudioDispatcher,
                            onAnotherThread: Boolean = true,
                            playback: Boolean = false,
                            samplingRate: Int = Settings.samplingRate!!,
                            targetList: MutableList<Float>,
                            labelName: String = "Default",
                            color: Int = ColorTemplate.getHoloBlue()) {
        val pdh: PitchDetectionHandler = object: PitchDetectionHandler {

            private var silinceBegin: Long = -1

            override fun handlePitch(result: PitchDetectionResult?, event: AudioEvent?) {
                val pitch:Float = result!!.pitch
                Log.d(TAG, String.format("pitch is %f, probability: %f", pitch, result!!.probability))
                if (!this@AudioService.isRunning && pitch > 0) {
                    // 音声検出し始め
                    this@AudioService.isRunning = true
                }
//                this@AudioService.isRunning = false
                if (this@AudioService.isRunning) {
                    // 稼働中はピッチを保存
                    if (pitch > MAX_FREQ_THRESHOLD) {
//                        targetList.add(targetList.last())
                    } else {
                        targetList.add(pitch)
                    }

                    if (pitch < 0) {
                        // 無音の場合無音開始時間をセット
                        if (this.silinceBegin == -1L) {
                            this.silinceBegin = System.currentTimeMillis()
                        }
                        // N秒以上無音なら停止
                        if ((System.currentTimeMillis() - this.silinceBegin) >= STOP_RECORDING_AFTER_SECOND * 1000) {
                            this@AudioService.stopRecord()
                        }
                    } else {
                        // 無音開始時間をクリア
                        this.silinceBegin = -1
                    }
                }
                this@AudioService.activity.runOnUiThread {
                    chart.addEntry(pitch, name = labelName, color = color)
                }
            }
        }
        val processor: AudioProcessor = object : PitchProcessor(Settings.algorithm, samplingRate.toFloat(), BUFFER_SIZE, pdh) {
            override fun processingFinished() {
                super.processingFinished()
                this@AudioService.isRunning = false
            }
        }

        dispatcher.addAudioProcessor(processor)

        if (playback) {
            val bufferSize = AudioTrack.getMinBufferSize(dispatcher.format.sampleRate.toInt(), AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
            dispatcher.addAudioProcessor(AndroidAudioPlayer(dispatcher.getFormat(), bufferSize, AudioManager.STREAM_MUSIC))
        }

        this.audioDispatcher = dispatcher
        if (onAnotherThread) {
            // TODO: Handlerにすべき？
            this.analyzeThread = Thread(dispatcher, "AudioDispatcher")
            this.analyzeThread!!.start()
        } else {
            dispatcher.run()
        }

    }

    private fun stopRecord() {
        this.audioDispatcher!!.stop()
        this.analyzeThread!!.interrupt()
        this.isRunning = false
        this@AudioService.activity.runOnUiThread {
            this.activity.button.text = "開始"
        }
    }

}