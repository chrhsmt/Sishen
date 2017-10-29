package com.chrhsmt.sisheng

import com.chrhsmt.sisheng.point.Point

/**
 * Created by hkimura on 2017/10/30.
 */
interface AudioServiceInterface {
    fun startAudioRecord()
    fun testPlay(fileName: String)
    fun attemptPlay(fileName: String)
    fun stop()
    fun analyze() : Point
    fun analyze(klassName: String) : Point
    fun clear()
    fun isRunning(): Boolean
}