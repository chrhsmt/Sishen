package com.chrhsmt.sisheng.ui

import android.app.Activity
import android.graphics.Color
import com.chrhsmt.sisheng.AudioService
import com.chrhsmt.sisheng.MainActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate

/**
 * Created by chihiro on 2017/08/22.
 */
class Chart {

    private val AXIS_MAXIMUM = AudioService.MAX_FREQ_THRESHOLD // Maximum Hz
    private val AXIS_MINIMUM = 0f
    private val activity: Activity
    private var mChart: LineChart? = null

    constructor(activity: Activity) {
        this.activity = activity
    }

    fun initChartView(chart: LineChart) {
        this.mChart = chart
        // enable touch gestures
        this.mChart!!.setTouchEnabled(true);

        // enable scaling and dragging
        this.mChart!!.setDragEnabled(true);
        this.mChart!!.setScaleEnabled(true);
        this.mChart!!.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        this.mChart!!.setPinchZoom(true)

        // set an alternative background color
        this.mChart!!.setBackgroundColor(Color.LTGRAY)

        val data = LineData()
        data.setValueTextColor(Color.BLACK)
        // add empty data
        this.mChart!!.data = data

        //  ラインの凡例の設定
        val l = this.mChart!!.getLegend()
        l.form = Legend.LegendForm.LINE
        l.textColor = Color.BLACK

        val xl = this.mChart!!.getXAxis()
        xl.textColor = Color.BLACK
        //xl.setLabelsToSkip(9)

        val leftAxis = this.mChart!!.getAxisLeft()
        leftAxis.textColor = Color.BLACK
        leftAxis.axisMaximum = AXIS_MAXIMUM
        leftAxis.axisMinimum = AXIS_MINIMUM
        leftAxis.setDrawGridLines(true)

        val rightAxis = this.mChart!!.getAxisRight()
        rightAxis.isEnabled = false

    }

    fun stop() {
        if (this.thread != null) {
            this.thread!!.interrupt()
            this.thread = null
        }
    }

    fun addEntry(value: Float, name: String = "Default Data", color: Int = ColorTemplate.getHoloBlue()) {

        val data = this.mChart!!.data
        if (data != null) {
            var set = data.getDataSetByLabel(name, false)
            if (set == null) {
                set = this.createDefaultSet(name = name, color = color)
                data.addDataSet(set)
            }

            var checkedValue = value
            if (checkedValue > AXIS_MAXIMUM) {
                checkedValue = 0f;
            }
            data.addEntry(Entry(set.entryCount.toFloat(), checkedValue), data.getIndexOfDataSet(set));
            data.notifyDataChanged()

            this.mChart!!.notifyDataSetChanged()
            this.mChart!!.setVisibleXRangeMaximum(120f)
            this.mChart!!.moveViewToX(set.entryCount.toFloat())
        }
    }

    fun clear() {
        this.mChart!!.clearValues()
        this.mChart!!.notifyDataSetChanged()
    }

    /**
     * 特定のデータアセットをクリア.
     * @name データセット名
     */
    fun clearDateSet(name: String) {
        val set = this.mChart!!.data.getDataSetByLabel(name, false)
        set?.let {
            set.clear()
        }
    }

    private fun createDefaultSet(name: String = "Default Data", color: Int = ColorTemplate.getHoloBlue()): LineDataSet {

        val set = LineDataSet(null, name)
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.color = color
        set.setCircleColor(color)
        set.lineWidth = 2f
        set.circleRadius = 4f
        set.fillAlpha = 65
        set.fillColor = color
        set.highLightColor = Color.rgb(244, 117, 117)
        set.valueTextColor = Color.WHITE
        set.valueTextSize = 9f
        set.setDrawValues(false)
        return set
    }

    // temporary thread
    private var thread: Thread? = null
    private fun feedMultiple() {
        if (this.thread != null) {
            this.thread!!.interrupt()
            this.thread = null
        }
        val runnable: Runnable = Runnable {
            addEntry((Math.random() * 40).toFloat())
        }

        this.thread = Thread(Runnable({
            for (i in 1..100) {
                this.activity.runOnUiThread(runnable)
                try {
                    Thread.sleep(100)
                } catch (exception: InterruptedException) {
                    exception.printStackTrace()
                }
            }
        }))
        this.thread!!.start()
    }

}