package com.chrhsmt.sisheng

import android.Manifest.permission.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.requestPermissions
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import be.tarsos.dsp.pitch.PitchProcessor
import com.github.mikephil.charting.charts.LineChart
import com.chrhsmt.sisheng.ui.Chart
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private val TAG: String = "MainActivity"
    private val PERMISSION_REQUEST_CODE = 1

    private var service: AudioService? = null
    private var mChart: LineChart? = null
    private var chart: Chart? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        this.chart = Chart(this)
        this.chart!!.initChartView(this.findViewById<LineChart>(R.id.chart))
        this.service = AudioService(this.chart!!, this)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        if (ActivityCompat.checkSelfPermission(this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(this, arrayOf(RECORD_AUDIO), PERMISSION_REQUEST_CODE)
            return
        }

        button.setOnClickListener { view ->
            if (button.text == "停止") {
                button.text = "開始"
                this.service!!.stop()
            } else {
                button.text = "停止"
                this.service!!.startAudioRecord()
            }
        }


        test_play.setOnClickListener({ view ->
            val openRawResource = this.resources.openRawResource(R.raw.di22)
            val data: ByteArray = kotlin.ByteArray(openRawResource.available())
            openRawResource.read(data)
            this.service!!.testPlay(data)
        })


        algorithm.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, PitchProcessor.PitchEstimationAlgorithm.values())
        algorithm.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val item: PitchProcessor.PitchEstimationAlgorithm = parent!!.getItemAtPosition(position) as PitchProcessor.PitchEstimationAlgorithm
                Settings.algorithm = item
                Toast.makeText(this@MainActivity, String.format("%sが選択されました", item.toString()), Toast.LENGTH_SHORT).show()
            }
        }

        sampling_rate.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, this.resources.getIntArray(R.array.sampling_rates).toList())
        sampling_rate.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val samplingRate = resources.getIntArray(R.array.sampling_rates)[position]
                Settings.samplingRate = samplingRate
                Toast.makeText(this@MainActivity, String.format("%dが選択されました", samplingRate), Toast.LENGTH_SHORT).show()
            }

        }

        analyze_button.setOnClickListener({ view ->
            val info = this@MainActivity.service!!.analyze()
            Toast.makeText(
                    this@MainActivity,
                    String.format("distance: %f, normalizedDistance: %f", info.distance, info.normalizedDistance),
                    Toast.LENGTH_LONG).show()
        })

        clear_button.setOnClickListener({ view ->
            this@MainActivity.service!!.clear()
            this@MainActivity.chart!!.clear()
        })
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    this.service!!.startAudioRecord()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        this.chart!!.stop()
    }
}
