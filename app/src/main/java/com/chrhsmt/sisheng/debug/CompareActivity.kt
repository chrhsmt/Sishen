package com.chrhsmt.sisheng.debug

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.TextView
import android.widget.Toast
import com.chrhsmt.sisheng.*
import com.chrhsmt.sisheng.exception.AudioServiceException
import com.chrhsmt.sisheng.font.FontUtils
import com.chrhsmt.sisheng.network.RaspberryPi
import com.chrhsmt.sisheng.point.SimplePointCalculator
import com.chrhsmt.sisheng.ui.Chart
import com.chrhsmt.sisheng.ui.ScreenUtils
import com.github.mikephil.charting.charts.LineChart
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_analyze_compare.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class CompareActivity : AppCompatActivity() {

    private val TAG: String = "CompareActivity"
    private val PERMISSION_REQUEST_CODE = 1

    private var service: AudioServiceInterface? = null
    private var chart: Chart? = null

    private var isRecording = false
    enum class REIBUN_STATUS(val rawValue: Int) {
        PREPARE(1),
        NORMAL(2),
        PLAYING(3),
        RECODING(4),
        ANALYZING(5),
        ANALYZE_FINISH(6),
        ANALYZE_ERROR_OCCUR(7),
    }
    private var nowStatus: REIBUN_STATUS = REIBUN_STATUS.NORMAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analyze_compare)

        // フルスクリーンにする
        ScreenUtils.setFullScreen(this.window)
        ScreenUtils.setScreenBackground(this)

        // ピンイン、中文、英文の配置
        val reibunInfo = ReibunInfo.getInstance(this)
        txtDebugPinyin.text = ReibunInfo.replaceNewLine(reibunInfo.selectedItem!!.pinyin)
        txtDebugChinese.text = ReibunInfo.replaceNewLine(reibunInfo.selectedItem!!.chinese)

        // 音声再生、録画の準備
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_CODE)
            //return
        }

        Settings.setDefaultValue(this, false)

        // グラフの準備
        this.chart = Chart()
        this.chart!!.initChartView(this.findViewById<LineChart>(R.id.chart))
        this.service = AudioService(this.chart!!, this)

        // プログレスダイアログを表示する
        val dialog = SpotsDialog(this@CompareActivity, getString(R.string.screen6_2), R.style.CustomSpotDialog)
        dialog.show()
        ScreenUtils.setFullScreen(dialog.window)

        // お手本事前再生
        nowStatus = REIBUN_STATUS.PREPARE
        updateButtonStatus()
        val fileName = reibunInfo.selectedItem!!.getMFSZExampleAudioFileName()
        Settings.sampleAudioFileName = fileName
        this.service!!.testPlay(fileName, playback = false, callback = object : Runnable {
            override fun run() {
                this@CompareActivity.runOnUiThread {

                    // DEBUG解析
                    val analyzed = AnalyzedRecordedData.getSelected()
                    if (analyzed != null) {
                        val file = analyzed.file
                        val path= SDCardManager().copyAudioFile(file, this@CompareActivity)
                        this@CompareActivity.service!!.debugTestPlay(file.name, path, callback = object : Runnable {
                            override fun run() {
                                val point = this@CompareActivity.service?.analyze()
                                val calcurator = SimplePointCalculator()
                                calcurator.setCalibrationType(SimplePointCalculator.Companion.CALIBRATION_TYPE.FREQ)
                                calcurator.setNoiseReducer(SimplePointCalculator.Companion.NOISE_RECUDER.V2)
                                val v2Point = (this@CompareActivity.service as AudioService)?.analyze(calcurator)

                                this@CompareActivity.runOnUiThread {
                                    (this@CompareActivity.service as? AudioService)?.addOtherChart(
                                            point?.analyzedFreqList,
                                            "男女設定キャリブレーション",
                                            Color.rgb(10, 255, 10))
                                    (this@CompareActivity.service as? AudioService)?.addOtherChart(
                                            v2Point?.analyzedFreqList,
                                            "周波数キャリブレーション",
                                            Color.rgb(255, 10, 255))
                                    txtScore.text = String.format("Point: %s, F-Point: %s", point?.score, v2Point?.score)
                                }
                            }
                        })
                    }

                    nowStatus = REIBUN_STATUS.NORMAL
                    updateButtonStatus()
                    dialog.dismiss()
                }
            }
        })

        // お手本再生
        btnAnalyzeOtehon.setOnClickListener({
            nowStatus = CompareActivity.REIBUN_STATUS.PLAYING
            updateButtonStatus()

            this@CompareActivity.service!!.clearTestFrequencies()
            this@CompareActivity.chart!!.clear()

            val fileName = reibunInfo.selectedItem!!.getMFSZExampleAudioFileName()
            Log.d(TAG, "Play " + fileName)
            this.service!!.testPlay(fileName, callback = object : Runnable {
                override fun run() {
                    Thread.sleep(300)

                    this@CompareActivity.runOnUiThread {

                        // DEBUG解析
                        val analyzed = AnalyzedRecordedData.getSelected()
                        if (analyzed != null) {
                            val file = analyzed.file
                            val path= SDCardManager().copyAudioFile(file, this@CompareActivity)
                            this@CompareActivity.service!!.clearFrequencies()
                            this@CompareActivity.service!!.debugTestPlay(file.name, path, playback = true, callback = object : Runnable {
                                override fun run() {
                                    val point = this@CompareActivity.service?.analyze()
                                    val calcurator = SimplePointCalculator()
                                    calcurator.setCalibrationType(SimplePointCalculator.Companion.CALIBRATION_TYPE.FREQ)
                                    calcurator.setNoiseReducer(SimplePointCalculator.Companion.NOISE_RECUDER.V2)
                                    val v2Point = (this@CompareActivity.service as AudioService)?.analyze(calcurator)

                                    this@CompareActivity.runOnUiThread {
                                        (this@CompareActivity.service as? AudioService)?.addOtherChart(
                                                point?.analyzedFreqList,
                                                "男女設定キャリブレーション",
                                                Color.rgb(10, 255, 10))
                                        (this@CompareActivity.service as? AudioService)?.addOtherChart(
                                                v2Point?.analyzedFreqList,
                                                "周波数キャリブレーション",
                                                Color.rgb(255, 10, 255))
                                        txtScore.text = String.format("Point: %s, F-Point: %s", point?.score, v2Point?.score)

                                        this@CompareActivity.nowStatus = REIBUN_STATUS.NORMAL
                                        this@CompareActivity.updateButtonStatus()
                                        dialog.dismiss()
                                    }
                                }
                            })
                        }
                    }
                }
            })

            Thread(Runnable {
                Thread.sleep(2000)
                when (this@CompareActivity.service!!.isRunning()) {
                    true -> Thread.sleep(1000)
                }

                this@CompareActivity.runOnUiThread {
                    nowStatus = CompareActivity.REIBUN_STATUS.NORMAL
                    updateButtonStatus()
                }
            }).start()
        })

        // タイトル長押下された場合は、デバッグ画面に遷移する。
        if (Settings.DEBUG_MODE) {
            txtDebugReibun.setOnLongClickListener(View.OnLongClickListener {
                val intent = Intent(this@CompareActivity, MainActivity::class.java)
                startActivity(intent)
                true
            })
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        ScreenUtils.setFullScreen(this@CompareActivity.window)
    }

    fun analyze() {
        // プログレスダイアログを表示する
        val dialog = SpotsDialog(this@CompareActivity, getString(R.string.screen6_3), R.style.CustomSpotDialog)
        dialog.show()
        FontUtils.changeFont(this@CompareActivity, dialog.findViewById<TextView>(dmax.dialog.R.id.dmax_spots_title), 1.1f)
        ScreenUtils.setFullScreen(dialog.window)

        // スレッドを開始する
        Thread(Runnable {
            Thread.sleep(1000 * 3)

            try {
                analyzeInner()
            } catch (e: AudioServiceException) {
                Log.e(TAG, e.message)
                runOnUiThread {
                    dialog.dismiss()
                    txtDebugError.visibility = View.VISIBLE

                    nowStatus = REIBUN_STATUS.ANALYZE_ERROR_OCCUR
                    updateButtonStatus()
                }
            }
        }).start()
    }

    @Throws(AudioServiceException::class)
    private fun analyzeInner() {
        val info = this@CompareActivity.service!!.analyze()
//        val info2 = this@ReibunActivity.service!!.analyze(FreqTransitionPointCalculator::class.qualifiedName!!)
//        val info = this@ReibunActivity.service!!.analyze(NMultiplyLogarithmPointCalculator::class.qualifiedName!!)
        if (info.success()) {
            RaspberryPi().send(object: Callback {
                override fun onFailure(call: Call?, e: IOException?) {
                    runOnUiThread {
                        Toast.makeText(this@CompareActivity, e!!.message, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onResponse(call: Call?, response: Response?) {
                    runOnUiThread {
                        Toast.makeText(this@CompareActivity, response!!.body()!!.string(), Toast.LENGTH_LONG).show()
                    }
                }
            })
        }

        runOnUiThread {
            nowStatus = REIBUN_STATUS.ANALYZE_FINISH
            updateButtonStatus()

            val intent = Intent(this@CompareActivity, ResultActivity::class.java)
            intent.putExtra("result", info.success())
            intent.putExtra("score", info.score.toString())
            startActivity(intent)
            overridePendingTransition(0, 0);
        }
    }

    private fun updateButtonStatus() {
        when (nowStatus) {
            REIBUN_STATUS.PREPARE -> {
                // 録音ボタン：録音可、再生ボタン：再生可
                btnAnalyzeOtehon.setBackgroundResource(R.drawable.shape_round_button)
                btnAnalyzeOtehon.setEnabled(false)
            }
            REIBUN_STATUS.NORMAL -> {
                // 録音ボタン：録音可、再生ボタン：再生可
                btnAnalyzeOtehon.setBackgroundResource(R.drawable.shape_round_button)
                btnAnalyzeOtehon.setEnabled(true)
            }
            REIBUN_STATUS.PLAYING -> {
                // 録音ボタン：利用不可、再生ボタン：再生中
                btnAnalyzeOtehon.setBackgroundResource(R.drawable.shape_round_button_press)
                btnAnalyzeOtehon.setEnabled(false)
            }
            REIBUN_STATUS.RECODING -> {
                // 録音ボタン：録音中、再生ボタン：再生不可
                val alphaAnimation = AlphaAnimation(1.0f, 0.7f)
                alphaAnimation.duration = 1000
                alphaAnimation.fillAfter = true
                alphaAnimation.repeatCount = -1
                btnAnalyzeOtehon.setBackgroundResource(R.drawable.shape_round_button_disable)
                btnAnalyzeOtehon.setEnabled(false)
            }
            REIBUN_STATUS.ANALYZING -> {
                // 録音ボタン：録音不可、再生ボタン：再生不可
                btnAnalyzeOtehon.setBackgroundResource(R.drawable.shape_round_button_disable)
                btnAnalyzeOtehon.setEnabled(false)
            }
            REIBUN_STATUS.ANALYZE_FINISH -> {
                //ボタン等のパーツの状態を戻さずに結果画面に遷移する想定。
                //本画面に戻る時は、再度 onCreate から行われる想定。
            }
            REIBUN_STATUS.ANALYZE_ERROR_OCCUR -> {
                // 録音ボタン：録音可、再生ボタン：再生不可
                btnAnalyzeOtehon.setBackgroundResource(R.drawable.shape_round_button_disable)
                btnAnalyzeOtehon.setEnabled(false)
            }
        }
    }
}
