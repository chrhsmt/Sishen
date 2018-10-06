package com.chrhsmt.sisheng

import com.chrhsmt.sisheng.point.Point

/**
 * Created by hkimura on 2017/10/30.
 */
interface AudioServiceInterface {
    fun startAudioRecord()
    fun testPlay(fileName: String,
                 path: String? = null,
                 playback: Boolean = true,
                 callback: Runnable? = null,
                 async: Boolean = true,
                 labelName: String = "SampleAudio")
    fun debugTestPlay(fileName: String, path: String, playback: Boolean = false, callback: Runnable?)
    fun attemptPlay(fileName: String)
    fun stop()
    fun analyze() : Point
    fun analyze(klassName: String) : Point
    fun clear()
    fun clearTestFrequencies()
    fun clearFrequencies()
    fun isRunning(): Boolean
}