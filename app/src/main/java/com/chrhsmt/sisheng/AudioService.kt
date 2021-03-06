package com.chrhsmt.sisheng

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.util.Log
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.io.android.AndroidAudioPlayer
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchDetectionResult
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.writer.WriterProcessor
import com.chrhsmt.sisheng.exception.AudioServiceException
import com.chrhsmt.sisheng.persistence.ExternalMedia
import com.chrhsmt.sisheng.point.Point
import com.chrhsmt.sisheng.point.PointCalculator
import com.chrhsmt.sisheng.point.SimplePointCalculator
import com.chrhsmt.sisheng.ui.Chart
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.android.synthetic.main.content_main.*
import java.io.File
import java.io.RandomAccessFile
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by chihiro on 2017/08/22.
 */
class AudioService : AudioServiceInterface {

    companion object {
//        val SAMPLING_RATE: Int = 22050 // 44100
        val AUDIO_FILE_SAMPLING_RATE: Int = 44100
        // 録音時に指定秒数の空白時間後に録音停止
        val STOP_RECORDING_AFTER_SECOND: Int = 2
        val BUFFER_SIZE: Int = 1024
        val MAX_FREQ_THRESHOLD = 500f
        val MICROPHONE_DATA_SET_LABEL_NAME = "Microphone"
    }

    private val TAG: String = "AudioService"

    private val activity: Activity
    private var chart: Chart? = null
    private var audioDispatcher: AudioDispatcher? = null
    private var analyzeThread: Thread? = null
    private var isRunning: Boolean = false

    private var frequencies: MutableList<Float> = ArrayList<Float>()
    private var testFrequencies: MutableList<Float> = ArrayList<Float>()

    constructor(chart: Chart?, activity: Activity) {
        this.activity = activity
        this.chart = chart
        // Setting ffmpeg
        AndroidFFMPEGLocator(this.activity)
    }

    override fun startAudioRecord() {
        // 既存データをクリア
        this.frequencies.clear()
        this.chart?.clearDateSet(MICROPHONE_DATA_SET_LABEL_NAME)


        // マイクロフォンバッファサイズの計算
        val microphoneBufferSize = AudioRecord.getMinBufferSize(
                Settings.samplingRate!!,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT) / 2
        this.startRecord(
                AudioDispatcherFactory.fromDefaultMicrophone(Settings.samplingRate!!, microphoneBufferSize, 0),
                targetList = this.frequencies,
                labelName = MICROPHONE_DATA_SET_LABEL_NAME,
                color = Color.rgb(10, 240, 10),
                shouldRecord = true
        )
    }

    @SuppressLint("WrongConstant")
    override fun testPlay(fileName: String, path: String?, playback: Boolean, callback: Runnable?, async: Boolean, labelName: String) {

        this.testFrequencies.clear()
        this.chart?.clear()

        val run = Runnable {

            val audioPath: String = if (path == null) {
                this.copyAudioFile(fileName)
            } else {
                path
            }

            this.startRecord(
                    AudioDispatcherFactory.fromPipe(
                            audioPath,
                            AUDIO_FILE_SAMPLING_RATE,
                            BUFFER_SIZE,
                            0
                    ),
                    false,
                    playback = playback,
                    samplingRate = AUDIO_FILE_SAMPLING_RATE,
                    targetList = this.testFrequencies,
                    labelName = labelName,
                    callback = callback
            )
        }
        if (async) {
            Thread(run).start()
        } else {
            run.run()
        }

    }

    @SuppressLint("WrongConstant")
    override fun debugTestPlay(fileName: String, path: String, playback: Boolean, callback: Runnable?) {

        Thread(Runnable {

            this.startRecord(
                    AudioDispatcherFactory.fromPipe(
                            path,
                            AUDIO_FILE_SAMPLING_RATE,
                            BUFFER_SIZE,
                            0
                    ),
                    false,
                    playback = playback,
                    samplingRate = AUDIO_FILE_SAMPLING_RATE,
                    targetList = this.frequencies,
                    labelName = "RecordedSampleAudio",
                    callback = callback,
                    color = Color.rgb(255, 10, 10)
            )
        }).start()

    }

    @SuppressLint("WrongConstant")
    override fun attemptPlay(fileName: String) {

        AndroidFFMPEGLocator(this.activity)

        Thread(Runnable {

            val path =  this.copyAudioFile(fileName)

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

    override fun stop() {
        this.stopRecord()
    }

    @Throws(AudioServiceException::class)
    override fun analyze() : Point {
        return analyze(SimplePointCalculator::class.qualifiedName!!)
    }

    @Throws(AudioServiceException::class)
    override fun analyze(klassName: String) : Point {
        val calculator: PointCalculator = Class.forName(klassName).newInstance() as PointCalculator
        val point = calculator.calc(this.frequencies, this.testFrequencies)
        if (point.base <= this.testFrequencies.size) {
            // 録音が失敗している場合
            throw AudioServiceException("不好意思，我听不懂")
        }
        return point
    }

    @Throws(AudioServiceException::class)
    fun analyze(calculator: PointCalculator) : Point {
        val point = calculator.calc(this.frequencies, this.testFrequencies)
        if (point.base <= this.testFrequencies.size) {
            // 録音が失敗している場合
            throw AudioServiceException("不好意思，我听不懂")
        }
        return point
    }

    override fun clearTestFrequencies() {
        this.testFrequencies.clear()
    }

    override fun clearFrequencies() {
        this.frequencies.clear()
    }

    override fun clear() {
        this.frequencies.clear()
        this.testFrequencies.clear()
    }

    private fun startRecord(dispatcher: AudioDispatcher,
                            onAnotherThread: Boolean = true,
                            playback: Boolean = false,
                            samplingRate: Int = Settings.samplingRate!!,
                            targetList: MutableList<Float>,
                            labelName: String = "Default",
                            color: Int = ColorTemplate.getHoloBlue(),
                            callback: Runnable? = null,
                            shouldRecord: Boolean = false) {
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
                    chart?.addEntry(pitch, name = labelName, color = color)
                }
            }
        }
        val processor: AudioProcessor = object : PitchProcessor(Settings.algorithm, samplingRate.toFloat(), BUFFER_SIZE, pdh) {
            override fun processingFinished() {
                super.processingFinished()
                this@AudioService.isRunning = false
                callback?.let { block ->
                    block.run()
                }
            }
        }

        dispatcher.addAudioProcessor(processor)

        if (shouldRecord) {
            ExternalMedia.saveDir?.takeIf { it -> it.canWrite() }?.let { it ->
                val dateString = SimpleDateFormat("yyyy-MM-dd").format(Date())
                val directory = File(it, dateString)
                if (!directory.isDirectory) {
                    directory.mkdir()
                }
                val dateTimeString = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss").format(Date())
                val id = Regex("^mfsz/(\\d+)_[f|m].wav$").find(Settings.sampleAudioFileName!!)?.groups?.last()?.value
                val sex = Settings.sex!!.first().toLowerCase()
                val newFile = File(directory, String.format("%s-%s-%s.wav", dateTimeString, id, sex))
                val format = TarsosDSPAudioFormat(AUDIO_FILE_SAMPLING_RATE.toFloat(), 16, 1, true, false)
                val writeProcessor: AudioProcessor = WriterProcessor(format, RandomAccessFile(newFile, "rw"))
                dispatcher.addAudioProcessor(writeProcessor)
            }
        }

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
        this.audioDispatcher?.stop()
        this.analyzeThread?.interrupt()
        this.isRunning = false
        this@AudioService.activity.runOnUiThread {
            this.activity.button?.text = "開始"
        }
    }

    override fun isRunning(): Boolean {
        return this.isRunning
    }

    @SuppressLint("WrongConstant")
    private fun copyAudioFile(fileName: String): String {
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

        return path
    }

    fun getTestFreq(): MutableList<Float> {
        return this.testFrequencies
    }

    fun addOtherChart(freqs: MutableList<Float>?, labelName: String, color: Int) {
        freqs?.forEach { fl ->
            chart?.addEntry(fl, name = labelName, color = color)
        }
    }
}
