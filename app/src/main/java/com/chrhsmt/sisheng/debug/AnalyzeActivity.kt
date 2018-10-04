package com.chrhsmt.sisheng.debug

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.Toast
import com.chrhsmt.sisheng.*
import com.chrhsmt.sisheng.persistence.SDCardManager
import com.chrhsmt.sisheng.point.SimplePointCalculator
import com.chrhsmt.sisheng.ui.ScreenUtils
import dmax.dialog.SpotsDialog

import kotlinx.android.synthetic.main.activity_analyze.*
import java.io.File

class AnalyzeActivity : Activity() {

    companion object {
        val TAG = "AnalyzeActivity"
    }

    private var service: AudioService? = null
    private var files: List<File>? = null
    private var datas: List<AnalyzedRecordedData>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analyze)

        // フルスクリーンにする
        ScreenUtils.setFullScreen(this.window)
        ScreenUtils.setScreenBackground(this)

        Settings.setDefaultValue(this@AnalyzeActivity, true)

        this.service = AudioService(null, this)

        val cardManager: SDCardManager = SDCardManager()
        cardManager.setUpReadWriteExternalStorage(this)
        this.files = cardManager.getFileList()
        val lines = cardManager.readCsv(this@AnalyzeActivity, "result.csv")

        try {
            this.datas = this.files?.map { file ->
                val line = lines.find { line -> line.startsWith(file.name) }
                lines.minus(line)
                AnalyzedRecordedData(file, line).init()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // ファイルリスト
        val fileNames = this.datas?.map { data -> data.toString() }
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileNames)
        listFiles.adapter = adapter as ListAdapter

        listFiles.setOnItemClickListener { parent, view, position, id ->

            val reibunInfo = ReibunInfo.getInstance(this)
            val itemPosition = this.datas?.get(position)?.id?.minus(1) ?: run {
                Log.e(TAG, "Data not found !!!")
                return@setOnItemClickListener
            }
            reibunInfo.setSelectedItem(itemPosition)
            val data = this.datas!!.get(position)
            AnalyzedRecordedData.setSelected(data)
            Settings.sex = data.sex!!

            val intent = Intent(this@AnalyzeActivity, CompareActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0);
        }

        // タイトル長押下された場合は、デバッグ画面に遷移する。
        if (Settings.DEBUG_MODE) {
            analyze_title.setOnLongClickListener(View.OnLongClickListener {
                val intent = Intent(this@AnalyzeActivity, MainActivity::class.java)
                startActivity(intent)
                true
            })
        }

        btnAnalyze.setOnClickListener({ v: View? ->
            val dialog = SpotsDialog(this@AnalyzeActivity, getString(R.string.screen6_3), R.style.CustomSpotDialog)
            analyze_title.setText("解析開始します")
            // プログレスダイアログを表示する
            dialog.show()
            ScreenUtils.setFullScreen(dialog.window)
            runOnUiThread {
            }

            Toast.makeText(this@AnalyzeActivity, "解析開始します", Toast.LENGTH_SHORT).show()

            Settings.setDefaultValue(this@AnalyzeActivity, false)


            Thread(Runnable {
                var count = 0
                var map: MutableMap<String, MutableList<Float>> = HashMap<String, MutableList<Float>>()
                val reibunInfo = ReibunInfo.getInstance(this@AnalyzeActivity)
                reibunInfo.getItemList().forEach {
                    this@AnalyzeActivity.service?.testPlay(it.getMFSZExampleAudioFileName(), playback = false, async = false)
                    val sampleData = this@AnalyzeActivity.service?.getTestFreq()
                    map[it.id.toString()] = sampleData!!.toMutableList()
                }

                var buffer = StringBuilder()
                var nullMaleBuffer = StringBuilder()
                var nullFemaleBuffer = StringBuilder()

                this@AnalyzeActivity.files?.forEach { file ->
                    val fileName = file.name

                    // ファイル名から条件取得
                    ".*-(.*)-(.*)\\.wav".toRegex().find(fileName)?.groups?.let { it ->
                        val id = it.get(1)?.value
                        val sex = it.get(2)?.value
                        if (id != null && !"null".equals(id)) {
                            val position = id.toInt().minus(1)
                            reibunInfo.setSelectedItem(position)
                            val reibunName = reibunInfo.selectedItem!!.getMFSZExampleAudioFileName()
                            Settings.sampleAudioFileName = reibunName

                            if (sex == "null") {
                                // male
                                Settings.sex = "m"
                                this@AnalyzeActivity.extractLastYearData(file, nullMaleBuffer, map[id]!!, id, "m", fileName)
                                // female
                                Settings.sex = "f"
                                this@AnalyzeActivity.extractLastYearData(file, nullFemaleBuffer, map[id]!!, id, "f", fileName)
                            } else {
                                Settings.sex = sex
                                this@AnalyzeActivity.extractLastYearData(file, buffer, map[id]!!, id, sex!!, fileName)
                            }
                            count++
                        }
                    }

                }

                // file書き出し？
                SDCardManager().write(this@AnalyzeActivity, "result.csv", buffer.toString())
                if (nullMaleBuffer.isNotEmpty()) {
                    SDCardManager().write(this@AnalyzeActivity, "nullMaleResult.csv", nullMaleBuffer.toString())
                }
                if (nullFemaleBuffer.isNotEmpty()) {
                    SDCardManager().write(this@AnalyzeActivity, "nullFemaleResult.csv", nullFemaleBuffer.toString())
                }

                Log.i(TAG, "finished")
                Toast.makeText(this@AnalyzeActivity, String.format("解析完了: %d件", count), Toast.LENGTH_LONG).show()
                runOnUiThread {
                    dialog.dismiss()
                    analyze_title.text = String.format("解析完了: %d件", count)
                }
            }).start()
        })
    }

    private fun extractLastYearData(file: File, buffer: StringBuilder, sampleData: MutableList<Float>, id: String, sex: String, fileName: String) {
        val cardManager: SDCardManager = SDCardManager()
        // 去年のデータ抽出
        val path= cardManager.copyAudioFile(file, this@AnalyzeActivity)
        this@AnalyzeActivity.service?.testPlay(file.name, path, playback = false, callback = Runnable {

            val data = this@AnalyzeActivity.service?.getTestFreq()

            val calculator: SimplePointCalculator = SimplePointCalculator()
            calculator.setV1()
            val point = calculator.calc(data!!, sampleData)
            val sexBaseScore= point.score
            val sexBaseSuccess= point.success()

            calculator.setV2()
            val freqPoint = calculator.calc(data!!, sampleData)
            buffer.append(fileName + ", " + id + ", " + sex + ", " + sexBaseScore + ", " + sexBaseSuccess + ", " + freqPoint.score +  "\n")

        }, async = false)
    }

}
