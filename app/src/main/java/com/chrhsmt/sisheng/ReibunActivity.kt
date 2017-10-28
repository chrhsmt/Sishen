package com.chrhsmt.sisheng

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import be.tarsos.dsp.pitch.PitchProcessor
import com.chrhsmt.sisheng.ui.Chart
import com.github.mikephil.charting.charts.LineChart
import kotlinx.android.synthetic.main.activity_reibun.*
import kotlinx.android.synthetic.main.content_main.*

class ReibunActivity : AppCompatActivity() {

    private val TAG: String = "ReibunActivity"
    private val PERMISSION_REQUEST_CODE = 1

    private var service: AudioService? = null
    private var mChart: LineChart? = null
    private var chart: Chart? = null

    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reibun)

        // フルスクリーンにする
        val decor = this.window.decorView
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        val reibunInfo = ReibunInfo.getInstance(this)
        txtPinyin.setText(reibunInfo.selectedItem?.pinyin)
        txtChinese.setText(reibunInfo.selectedItem?.chinese)
        txtEnglish.setText(reibunInfo.selectedItem?.english)

        val audioName = resources.getStringArray(R.array.sample_audios)[reibunInfo.selectedItem!!.id % 6]
        Settings.sampleAudioFileName = audioName

        this.chart = Chart(this)
        this.chart!!.initChartView(this.findViewById<LineChart>(R.id.chart))
        this.service = AudioService(this.chart!!, this)

        // お手本再生
        btnOtehon.setOnClickListener(View.OnClickListener {
            this@ReibunActivity.service!!.clear()
            this@ReibunActivity.chart!!.clear()
            btnOtehon.setBackgroundResource(R.drawable.now_playing_button)
            Log.d(TAG, "Play " + Settings.sampleAudioFileName)
            this.service!!.testPlay(Settings.sampleAudioFileName!!)

            Thread(Runnable {
                Thread.sleep(2000)
                when (this@ReibunActivity.service!!.isRunning()) {
                    true -> Thread.sleep(1000)
                }

                this@ReibunActivity.runOnUiThread {
                    btnOtehon.setBackgroundResource(R.drawable.play_button)
                }
            }).start()
        })


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_CODE)
            //return
        }

        Settings.algorithm = PitchProcessor.PitchEstimationAlgorithm.YIN
        Settings.samplingRate = resources.getIntArray(R.array.sampling_rates)[0]

        // 録音ボタン
        isRecording = false
        btnRokuon.setOnClickListener(View.OnClickListener {
            if (isRecording == true) {
                btnRokuon.setBackgroundResource(R.drawable.record_button)
                this.service!!.stop()
                isRecording = false
            } else {
                this@ReibunActivity.service!!.clear()
                this@ReibunActivity.chart!!.clear()
                btnRokuon.setBackgroundResource(R.drawable.now_recording_button)
                this.service!!.startAudioRecord()
                isRecording = true
            }
        })

        txtReibun.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@ReibunActivity, MainActivity::class.java)
            startActivity(intent)
        })
    }
}
