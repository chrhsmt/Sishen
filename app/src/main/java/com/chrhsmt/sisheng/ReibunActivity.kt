package com.chrhsmt.sisheng

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import com.chrhsmt.sisheng.exception.AudioServiceException
import com.chrhsmt.sisheng.network.RaspberryPi
import com.chrhsmt.sisheng.point.FreqTransitionPointCalculator
import com.chrhsmt.sisheng.point.NMultiplyLogarithmPointCalculator
import com.chrhsmt.sisheng.ui.Chart
import com.github.mikephil.charting.charts.LineChart
import kotlinx.android.synthetic.main.activity_reibun.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import javax.xml.transform.Result

class ReibunActivity : AppCompatActivity() {

    private val TAG: String = "ReibunActivity"
    private val PERMISSION_REQUEST_CODE = 1

    private var service: AudioServiceInterface? = null
    private var mChart: LineChart? = null
    private var chart: Chart? = null

    private var isRecording = false
    enum class REIBUN_STATUS(val rawValue: Int) {
        NORMAL(1),
        PLAYING(2),
        RECODING(3),
        ANALYZING(4),
        ANALYZE_FINISH(5),
        ANALYZE_ERROR_OCCUR(6),
    }
    private var nowStatus: REIBUN_STATUS = REIBUN_STATUS.NORMAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reibun)

        // フルスクリーンにする
        val decor = this.window.decorView
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        // ピンイン、中文、英文の配置
        val reibunInfo = ReibunInfo.getInstance(this)
        txtPinyin.setText(reibunInfo.selectedItem?.pinyin)
        txtChinese.setText(reibunInfo.selectedItem?.chinese)
        txtEnglish.setText(reibunInfo.selectedItem?.english)

        // 音声再生、録画の準備
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_CODE)
            //return
        }

        val audioName = resources.getStringArray(R.array.sample_audios)[reibunInfo.selectedItem!!.id % 5]
        Settings.sampleAudioFileName = audioName
        Settings.setDefaultValue(this, false)

        // グラフの準備
        this.chart = Chart(this)
        this.chart!!.initChartView(this.findViewById<LineChart>(R.id.chart))
        if (Settings.EMULATOR_MODE) {
            this.service = AudioServiceMock(this.chart!!, this)
        } else {
            this.service = AudioService(this.chart!!, this)
        }


        // お手本再生
        btnOtehon.setOnClickListener(View.OnClickListener {
            nowStatus = REIBUN_STATUS.PLAYING
            updateButtonStatus()

            this@ReibunActivity.service!!.clear()
            this@ReibunActivity.chart!!.clear()

            val fileName = reibunInfo.selectedItem!!.getMFSZExampleAudioFileName()
            Log.d(TAG, "Play " + fileName)
            this.service!!.testPlay(fileName)

            Thread(Runnable {
                Thread.sleep(2000)
                when (this@ReibunActivity.service!!.isRunning()) {
                    true -> Thread.sleep(1000)
                }

                this@ReibunActivity.runOnUiThread {
                    nowStatus = REIBUN_STATUS.NORMAL
                    updateButtonStatus()
                }
            }).start()
        })

        // 録音ボタン
        isRecording = false
        btnRokuon.setOnClickListener(View.OnClickListener {
            txtError.visibility = View.INVISIBLE

            if (isRecording == true) {
                nowStatus = REIBUN_STATUS.ANALYZING
                updateButtonStatus()

                this.service!!.stop()
                isRecording = false

                analyze()
            } else {
                nowStatus = REIBUN_STATUS.RECODING
                updateButtonStatus()

//                this@ReibunActivity.service!!.clear()
//                this@ReibunActivity.chart!!.clear()
                this.service!!.startAudioRecord()
                isRecording = true
            }
        })

        // タイトル長押下された場合は、デバッグ画面に遷移する。
        if (Settings.DEBUG_MODE) {
            txtReibun.setOnLongClickListener(View.OnLongClickListener {
                val intent = Intent(this@ReibunActivity, MainActivity::class.java)
                startActivity(intent)
                true
            })
        }
    }

    fun analyze() {
        // プログレスダイアログを表示する
        dialogAnalyzing.visibility = View.VISIBLE

        // スレッドを開始する
        Thread(Runnable {
            Thread.sleep(1000 * 3)

            try {
                analyzeInner()
            } catch (e: AudioServiceException) {
                runOnUiThread {
                    dialogAnalyzing.visibility = View.INVISIBLE
                    txtError.visibility = View.VISIBLE

                    nowStatus = REIBUN_STATUS.ANALYZE_ERROR_OCCUR
                    updateButtonStatus()
                }
            }
        }).start()
    }

    @Throws(AudioServiceException::class)
    private fun analyzeInner() {
//        val info = this@ReibunActivity.service!!.analyze()
//        val info2 = this@ReibunActivity.service!!.analyze(FreqTransitionPointCalculator::class.qualifiedName!!)
        val info3 = this@ReibunActivity.service!!.analyze(NMultiplyLogarithmPointCalculator::class.qualifiedName!!)
        if (info3.success()) {
            RaspberryPi().send(object: Callback {
                override fun onFailure(call: Call?, e: IOException?) {
                    runOnUiThread {
                        Toast.makeText(this@ReibunActivity, e!!.message, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onResponse(call: Call?, response: Response?) {
                    runOnUiThread {
                        Toast.makeText(this@ReibunActivity, response!!.body()!!.string(), Toast.LENGTH_LONG).show()
                    }
                }
            })
        }

        runOnUiThread {
            nowStatus = REIBUN_STATUS.ANALYZE_FINISH
            updateButtonStatus()

            val intent = Intent(this@ReibunActivity, ResultActivity::class.java)
            intent.putExtra("result", info3.success())
            intent.putExtra("score", info3.score.toString())
            startActivity(intent)
            overridePendingTransition(0, 0);
            /*
            Toast.makeText(
                    this@ReibunActivity,
                    String.format(
                            "score: %d\ndistance: %f, normalizedDistance: %f, base: %d, success: %s" +
                                    "\n" +
                                    "score: %d\ndistance: %f, normalizedDistance: %f, base: %d, success: %s",
                            info.score,
                            info.distance,
                            info.normalizedDistance,
                            info.base,
                            info.success().toString(),
                            info2.score,
                            info2.distance,
                            info2.normalizedDistance,
                            info2.base,
                            info2.success().toString()
                    ),
                    Toast.LENGTH_LONG
            ).show()
            */
        }
    }

    private fun updateButtonStatus() {
        when (nowStatus) {
            REIBUN_STATUS.NORMAL -> {
                // 録音ボタン：録音可、再生ボタン：再生可
                btnRokuon.setBackgroundResource(R.drawable.ic_record_button)
                btnRokuon.setEnabled(true)
                btnOtehon.setBackgroundResource(R.drawable.ic_play_button)
                btnOtehon.setEnabled(true)
            }
            REIBUN_STATUS.PLAYING -> {
                // 録音ボタン：利用不可、再生ボタン：再生中
                btnRokuon.setBackgroundResource(R.drawable.ic_record_button_disable)
                btnRokuon.setEnabled(false)
                btnOtehon.setBackgroundResource(R.drawable.ic_now_playing_button)
                btnOtehon.setEnabled(false)
            }
            REIBUN_STATUS.RECODING -> {
                // 録音ボタン：録音中、再生ボタン：再生不可
                btnRokuon.setBackgroundResource(R.drawable.ic_now_recording_button)
                btnRokuon.setEnabled(true)
                btnOtehon.setBackgroundResource(R.drawable.ic_play_button_disable)
                btnOtehon.setEnabled(false)
            }
            REIBUN_STATUS.ANALYZING -> {
                // 録音ボタン：録音不可、再生ボタン：再生不可
                btnRokuon.setBackgroundResource(R.drawable.ic_record_button_disable)
                btnRokuon.setEnabled(false)
                btnOtehon.setBackgroundResource(R.drawable.ic_play_button_disable)
                btnOtehon.setEnabled(false)
            }
            REIBUN_STATUS.ANALYZE_FINISH -> {
                //ボタン等のパーツの状態を戻さずに結果画面に遷移する想定。
                //本画面に戻る時は、再度 onCreate から行われる想定。
            }
            REIBUN_STATUS.ANALYZE_ERROR_OCCUR -> {
                // 録音ボタン：録音可、再生ボタン：再生不可
                btnRokuon.setBackgroundResource(R.drawable.ic_record_button)
                btnRokuon.setEnabled(true)
                btnOtehon.setBackgroundResource(R.drawable.ic_play_button_disable)
                btnOtehon.setEnabled(false)
            }
        }
    }
}


