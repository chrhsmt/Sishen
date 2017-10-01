package com.chrhsmt.sisheng

import android.annotation.SuppressLint
import android.content.Context
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



/**
 * Created by chihiro on 2017/08/22.
 */
class AudioService {

    companion object {
//        val SAMPLING_RATE: Int = 22050 // 44100
        val AUDIO_FILE_SAMPLING_RATE: Int = 44100
        // 録音時に指定秒数の空白時間後に録音停止
        val STOP_RECORDING_AFTER_SECOND: Int = 1
    }

    private val TAG: String = "AudioService"

    private val activity: MainActivity
    private val bufSize: Int = 1024
    private val chart: Chart
    private var audioDispatcher: AudioDispatcher? = null
    private var analyzeThread: Thread? = null
    private var isRunning: Boolean = false

    private var frequencies: MutableList<Float> = ArrayList<Float>()
    private var testFrequencies: MutableList<Float> = ArrayList<Float>()

    constructor(chart: Chart, activity: MainActivity) {
        this.activity = activity
        this.chart = chart
    }

    fun startAudioRecord() {
        // マイクロフォンバッファサイズの計算
        val microphoneBufferSize = AudioRecord.getMinBufferSize(
                Settings.samplingRate!!,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT) / 2
        this.startRecord(AudioDispatcherFactory.fromDefaultMicrophone(Settings.samplingRate!!, microphoneBufferSize, 0), targetList = this.frequencies)
    }

    @SuppressLint("WrongConstant")
    fun testPlay(data: ByteArray) {

        AndroidFFMPEGLocator(this.activity)

        // TODO: Handlerにすべき？
        Thread(Runnable {
            // ファイル移動
            val path = "/data/data/" + this.activity.packageName + "/files/di22.wav"
            val input = this.activity.assets.open("di22.wav")
            val output = this.activity.openFileOutput("di22.wav", Context.MODE_ENABLE_WRITE_AHEAD_LOGGING)
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
                            this.bufSize,
                            0
                    ),
                    false,
                    playback = true,
                    samplingRate = AUDIO_FILE_SAMPLING_RATE,
                    targetList = this.testFrequencies
            )
        }).start()

    }

    fun stop() {
        this.stopRecord()
    }

    private fun startRecord(dispatcher: AudioDispatcher,
                            onAnotherThread: Boolean = true,
                            playback: Boolean = false,
                            samplingRate: Int = Settings.samplingRate!!,
                            targetList: MutableList<Float>) {
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
                    targetList.add(pitch)

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
                chart.addEntry(pitch)
            }
        }
        val processor: AudioProcessor = PitchProcessor(Settings.algorithm, samplingRate.toFloat(), this.bufSize, pdh)
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
    }

}