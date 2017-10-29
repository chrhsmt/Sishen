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
import com.chrhsmt.sisheng.network.RaspberryPi
import com.chrhsmt.sisheng.point.FreqTransitionPointCalculator
import com.chrhsmt.sisheng.ui.Chart
import com.github.mikephil.charting.charts.LineChart
import kotlinx.android.synthetic.main.activity_reibun.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class ReibunActivity : AppCompatActivity() {

    private val TAG: String = "ReibunActivity"
    private val PERMISSION_REQUEST_CODE = 1

    private var service: AudioServiceInterface? = null
    private var mChart: LineChart? = null
    private var chart: Chart? = null

    private var isRecording = false

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
            //ボタンの状態を更新
            btnRokuon.setBackgroundResource(R.drawable.ic_record_button_disable)
            btnRokuon.setEnabled(false)
            btnOtehon.setBackgroundResource(R.drawable.ic_now_playing_button)
            btnOtehon.setEnabled(false)

            this@ReibunActivity.service!!.clear()
            this@ReibunActivity.chart!!.clear()
            Log.d(TAG, "Play " + Settings.sampleAudioFileName)
            this.service!!.testPlay(Settings.sampleAudioFileName!!)

            Thread(Runnable {
                Thread.sleep(2000)
                when (this@ReibunActivity.service!!.isRunning()) {
                    true -> Thread.sleep(1000)
                }

                this@ReibunActivity.runOnUiThread {
                    //ボタンの状態を更新
                    btnRokuon.setBackgroundResource(R.drawable.ic_record_button)
                    btnRokuon.setEnabled(true)
                    btnOtehon.setBackgroundResource(R.drawable.ic_play_button)
                    btnOtehon.setEnabled(true)
                }
            }).start()
        })

        // 録音ボタン
        isRecording = false
        btnRokuon.setOnClickListener(View.OnClickListener {
            if (isRecording == true) {
                //ボタンの状態を更新
                btnRokuon.setBackgroundResource(R.drawable.ic_record_button_disable)
                btnRokuon.setEnabled(false)
                btnOtehon.setBackgroundResource(R.drawable.ic_play_button_disable)
                btnOtehon.setEnabled(false)

                this.service!!.stop()
                isRecording = false

                analyze()
            } else {
                //ボタンの状態を更新
                btnRokuon.setBackgroundResource(R.drawable.ic_now_recording_button)
                btnRokuon.setEnabled(true)
                btnOtehon.setBackgroundResource(R.drawable.ic_play_button_disable)
                btnOtehon.setEnabled(false)

                this@ReibunActivity.service!!.clear()
                this@ReibunActivity.chart!!.clear()
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
            val info = this@ReibunActivity.service!!.analyze()
            val info2 = this@ReibunActivity.service!!.analyze(FreqTransitionPointCalculator::class.qualifiedName!!)
            if (info.success() && info2.success()) {
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
                //ボタンの状態を更新
                btnRokuon.setBackgroundResource(R.drawable.ic_record_button)
                btnRokuon.setEnabled(true)
                btnOtehon.setBackgroundResource(R.drawable.ic_play_button)
                btnOtehon.setEnabled(true)

                dialogAnalyzing.visibility = View.INVISIBLE
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
            }
        }).start()
    }
}
