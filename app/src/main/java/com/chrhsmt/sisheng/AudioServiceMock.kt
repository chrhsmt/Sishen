package com.chrhsmt.sisheng

import android.annotation.SuppressLint
import android.app.Activity
import com.chrhsmt.sisheng.exception.AudioServiceException
import com.chrhsmt.sisheng.point.Point
import com.chrhsmt.sisheng.point.SimplePointCalculator
import com.chrhsmt.sisheng.ui.Chart

/**
 * Created by hkimura on 2017/10/30.
 */
class AudioServiceMock : AudioServiceInterface {
    private val TAG: String = "AudioServiceMock"

    private val activity: Activity
    private val chart: Chart
    private var isRunning: Boolean = false

    constructor(chart: Chart, activity: Activity) {
        this.activity = activity
        this.chart = chart
    }

    override fun startAudioRecord() {
        // NOP
    }

    override fun testPlay(fileName: String, playback: Boolean, callback: Runnable?) {
        this.isRunning = true
    }

    override fun attemptPlay(fileName: String) {
        this.isRunning = true
    }

    override fun stop() {
        this.isRunning = false
    }

    @Throws(AudioServiceException::class)
    override fun analyze() : Point {
        return analyze("")
    }

    @Throws(AudioServiceException::class)
    override fun analyze(klassName: String) : Point {
        this.isRunning = true
        Thread.sleep(1000 * 2)
        this.isRunning = false
        //throw AudioServiceException()
        return Point(
                80,
                5.0,
                5.0,
                1)
    }

    override fun clear() {
        // NOP
    }

    override fun isRunning(): Boolean {
        return this.isRunning
    }
}