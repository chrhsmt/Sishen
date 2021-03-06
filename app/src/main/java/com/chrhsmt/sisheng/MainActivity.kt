package com.chrhsmt.sisheng

import android.Manifest.permission.RECORD_AUDIO
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.requestPermissions
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import be.tarsos.dsp.pitch.PitchProcessor
import com.chrhsmt.sisheng.exception.AudioServiceException
import com.chrhsmt.sisheng.network.RaspberryPi
import com.chrhsmt.sisheng.point.FreqTransitionPointCalculator
import com.chrhsmt.sisheng.point.NMultiplyLogarithmPointCalculator
import com.chrhsmt.sisheng.ui.Chart
import com.github.mikephil.charting.charts.LineChart
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException


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

//        Settings.raspberrypiHost = this.getString(R.string.default_pi_host) + ":" + context.getString(R.string.default_pi_port)
        Settings.raspberrypiPath = this.getString(R.string.default_pi_path)

        this.chart = Chart()
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
                button.text = "測定開始"
                this.service!!.stop()
            } else {
                button.text = "停止"
                this.service!!.startAudioRecord()
            }
        }


        /*
         * --------------------------------------------------------------------------------------
         * Play Example
         * --------------------------------------------------------------------------------------
         */
        test_play.setOnClickListener({ view ->
            this.service!!.testPlay(Settings.sampleAudioFileName!!)
        })


        /*
         * --------------------------------------------------------------------------------------
         * Setting algorithm
         * --------------------------------------------------------------------------------------
         */
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

        /*
         * --------------------------------------------------------------------------------------
         * Setting sampling rate
         * --------------------------------------------------------------------------------------
         */
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

        /*
         * --------------------------------------------------------------------------------------
         * Setting sex
         * --------------------------------------------------------------------------------------
         */
//        sex.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, this.resources.getIntArray(R.array.sampling_rates).toList())
        sex.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val sex = resources.getStringArray(R.array.sexes)[position]
                Settings.sex = sex
                Toast.makeText(this@MainActivity, String.format("%sが選択されました", sex), Toast.LENGTH_SHORT).show()
            }
        }

        /*
         * --------------------------------------------------------------------------------------
         * Setting sample audio for debug
         * --------------------------------------------------------------------------------------
         */
        sample_audios.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, this.resources.getStringArray(R.array.sample_audios).toList())
        sample_audios.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val audioName = resources.getStringArray(R.array.sample_audios)[position]
                Settings.sampleAudioFileName = audioName
                Toast.makeText(this@MainActivity, String.format("%sが選択されました", audioName), Toast.LENGTH_SHORT).show()
            }
        }

        /*
         * --------------------------------------------------------------------------------------
         * Setting mfsz sample audio for debug
         * --------------------------------------------------------------------------------------
         */
        val mfszList = listOf(
                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
                "9",
                "10",
                "11",
                "12",
                "13",
                "14",
                "15",
                "16",
                "17",
                "18",
                "19",
                "20",
                "s1",
                "s2",
                "s3",
                "s4",
                "s5",
                "s6",
                "s7",
                "s8",
                "s9",
                "s10"
        )
        mfsz_samples.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, mfszList)
        mfsz_samples.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val audioNamePrefix = mfszList[position]
                val audioName = this@MainActivity.assets.list("mfsz").first { asset -> asset.matches(Regex(String.format("%s_(f|m)\\.wav", audioNamePrefix))) }
                Settings.sampleAudioFileName = "mfsz/" + audioName
                Toast.makeText(this@MainActivity, String.format("%sが選択されました", audioNamePrefix), Toast.LENGTH_SHORT).show()
            }
        }

        /*
         * --------------------------------------------------------------------------------------
         * Analyze button
         * --------------------------------------------------------------------------------------
         */
        analyze_button.setOnClickListener({ view ->
            try {
                val info = this@MainActivity.service!!.analyze()
                val info2 = this@MainActivity.service!!.analyze(FreqTransitionPointCalculator::class.qualifiedName!!)
                val info3 = this@MainActivity.service!!.analyze(NMultiplyLogarithmPointCalculator::class.qualifiedName!!)
                if (info.success() && info2.success()) {
                    RaspberryPi().send(object: Callback {
                        override fun onFailure(call: Call?, e: IOException?) {
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, e!!.message, Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onResponse(call: Call?, response: Response?) {
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, response!!.body()!!.string(), Toast.LENGTH_LONG).show()
                            }
                        }
                    })
                }

                val message = String.format(
                        "[ log score ]: %d" +
                        "\n" +
                        "[ original ]:\n" +
                        "  score: %d\n  distance: %d, normalizedDistance: %d, base: %d, success: %s" +
                        "\n" +
                        "[ transiiton ]:\n" +
                        "  score: %d\n  distance: %d, normalizedDistance: %d, base: %d, success: %s"
                        ,
                        info3.score,
                        info.score,
                        info.distance.toInt(),
                        info.normalizedDistance.toInt(),
                        info.base,
                        info.success().toString(),
                        info2.score,
                        info2.distance.toInt(),
                        info2.normalizedDistance.toInt(),
                        info2.base,
                        info2.success().toString()
                )

    //            Toast.makeText(
    //                    this@MainActivity,
    //                    message,
    //                    Toast.LENGTH_LONG
    //            ).show()
                AlertDialog.Builder(this@MainActivity)
                        .setMessage(message)
                        .setTitle("Point")
                        .setPositiveButton("ok", null)
                        .show()

            } catch (e: AudioServiceException) {
                Log.e(TAG, e.message)
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }

        })

        /*
         * --------------------------------------------------------------------------------------
         * Clear button
         * --------------------------------------------------------------------------------------
         */
        clear_button.setOnClickListener({ view ->
            this@MainActivity.service!!.clear()
            this@MainActivity.chart!!.clear()
        })

        /*
         * --------------------------------------------------------------------------------------
         * Loading sample audio for debug
         * --------------------------------------------------------------------------------------
         */
        val dirs = this.resources.getStringArray(R.array.asset_dirs)
        recorded_sample_dir.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, dirs)
        recorded_sample_dir.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val dirName = dirs[position]
                Settings.recordedSampleAudioDir = dirName
                Toast.makeText(this@MainActivity, String.format("%sが選択されました", dirName), Toast.LENGTH_SHORT).show()
                setRecordedSampleSpinner()
            }
        }

        this.setRecordedSampleSpinner()

        attempt_play.setOnClickListener({ view ->
            this@MainActivity.service!!.attemptPlay(Settings.recordedSampleAudioFileName!!)
        })

        /*
         * --------------------------------------------------------------------------------------
         * Setting raspberry pi for debug
         * --------------------------------------------------------------------------------------
         */
        test_get.setOnClickListener({ view ->
            RaspberryPi().send(object: Callback {
                override fun onFailure(call: Call?, e: IOException?) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, e!!.message, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onResponse(call: Call?, response: Response?) {
                    response?.let {
                        if (response.isSuccessful) {
                            response.body()?.let { it ->
                                val text = it.string()
                                runOnUiThread {
                                    Toast.makeText(this@MainActivity, text, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
            })
        })

        pi_host.setText(Settings.raspberrypiHost)
        pi_path.setText(Settings.raspberrypiPath)
        pi_host.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(textComponent: Editable?) {
                Settings.raspberrypiHost = textComponent!!.toString()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(chars: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        pi_path.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(textComponent: Editable?) {
                Settings.raspberrypiPath = textComponent!!.toString()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(chars: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

    }

    private fun setRecordedSampleSpinner() {
        if (Settings.recordedSampleAudioDir.isNullOrBlank()) {
            return
        }
        val list = this.assets.list(Settings.recordedSampleAudioDir!!).map { asset -> String.format("%s/%s", Settings.recordedSampleAudioDir!!, asset) }
        recorded_sample.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, list)
        recorded_sample.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val audioName = list[position]
                Settings.recordedSampleAudioFileName = audioName
                Toast.makeText(this@MainActivity, String.format("%sが選択されました", audioName), Toast.LENGTH_SHORT).show()
            }
        }
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
    }
}
