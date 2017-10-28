package com.chrhsmt.sisheng

import android.content.Context
import be.tarsos.dsp.pitch.PitchProcessor

/**
 * Created by chihiro on 2017/09/26.
 */
object Settings {
    // 選択されたアルゴリズム
    var algorithm: PitchProcessor.PitchEstimationAlgorithm = PitchProcessor.PitchEstimationAlgorithm.YIN
    // サンプリングレート
    var samplingRate: Int? = null
    // 現在のユーザーの性別
    var sex: String? = null
    // 選択された例題音声
    var sampleAudioFileName: String? = null
    var recordedSampleAudioDir: String? = null
    var recordedSampleAudioFileName: String? = null

    // Raspberry pi
    var raspberrypiHost: String = ""
    var raspberrypiPath: String = ""

    // Debug Mode
    var DEBUG_MODE = true

    // Set Default Value if variable is not set.
    fun setDefaultValueIfNeed(context: Context) {
        if (Settings.algorithm == null) {
            Settings.algorithm = PitchProcessor.PitchEstimationAlgorithm.YIN
        }
        if (Settings.samplingRate == null) {
            Settings.samplingRate = context.resources.getIntArray(R.array.sampling_rates)[0]
        }
        if (Settings.sex == null) {
            Settings.sex = context.resources.getStringArray(R.array.sexes)[1]
        }
        if (Settings.raspberrypiHost == "") {
            Settings.raspberrypiHost = context.getString(R.string.default_pi_host)
        }
        if (Settings.raspberrypiPath == "") {
            Settings.raspberrypiPath = context.getString(R.string.default_pi_path)
        }

    }
}
