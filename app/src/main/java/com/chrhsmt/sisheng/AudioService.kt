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
import org.jtransforms.fft.DoubleFFT_1D
import java.util.logging.Handler
import android.media.AudioManager
import be.tarsos.dsp.io.android.AndroidAudioPlayer



/**
 * Created by chihiro on 2017/08/22.
 */
class AudioService {

    companion object {
        val SAMPLING_RATE: Int = 22050 // 44100
        val AUDIO_FILE_SAMPLING_RATE: Int = 44100
    }

    private val TAG: String = "AudioService"

    private val activity: MainActivity
    private val bufSize: Int = 1024
    private val chart: Chart
    private var audioDispatcher: AudioDispatcher? = null
    private var analyzeThread: Thread? = null
    private val microphoneBufferSize: Int

    constructor(chart: Chart, activity: MainActivity) {
        this.activity = activity
        this.chart = chart
        // マイクロフォンバッファサイズの計算
        this.microphoneBufferSize = AudioRecord.getMinBufferSize(
                SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT) / 2
    }

    fun startAudioRecord() {
        this.startAnalyze(AudioDispatcherFactory.fromDefaultMicrophone(SAMPLING_RATE, this.microphoneBufferSize, 0))
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

            this.startAnalyze(
                    AudioDispatcherFactory.fromPipe(
                            path,
                            AUDIO_FILE_SAMPLING_RATE,
                            this.bufSize,
                            0
                    ),
                    false,
                    playback = true
            )
        }).start()

    }

    fun stop() {
        this.stopAnalyze()
    }

    private fun startAnalyze(dispatcher: AudioDispatcher, onAnotherThread: Boolean = true, playback: Boolean = false) {
        val pdh: PitchDetectionHandler = object: PitchDetectionHandler {
            override fun handlePitch(result: PitchDetectionResult?, event: AudioEvent?) {
                val pitch:Float = result!!.pitch
                Log.d(TAG, String.format("pitch is %f, probability: %f", pitch, result!!.probability))
                chart.addEntry(pitch)
            }
        }
        val processor: AudioProcessor = PitchProcessor(Settings.algorithm, SAMPLING_RATE.toFloat(), this.bufSize, pdh)
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

    private fun stopAnalyze() {
        this.audioDispatcher!!.stop()
        this.analyzeThread!!.interrupt()
    }

}