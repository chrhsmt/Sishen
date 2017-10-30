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
    val DEBUG_MODE = true

    // Emulator Mode (音声再生、録音が動作しない)
    val EMULATOR_MODE = false

    // Set Default Value.
    fun setDefaultValue(context: Context, force:Boolean) {
        if (force) {
            algorithm = PitchProcessor.PitchEstimationAlgorithm.YIN
        }
        if (force || Settings.samplingRate == null) {
            samplingRate = context.resources.getIntArray(R.array.sampling_rates)[0]
        }
        if (force || Settings.sex == null) {
            sex = context.resources.getStringArray(R.array.sexes)[1]
        }
        if (force || Settings.raspberrypiHost == "") {
            raspberrypiHost = context.getString(R.string.default_pi_host)
        }
        if (force || Settings.raspberrypiPath == "") {
            raspberrypiPath = context.getString(R.string.default_pi_path)
        }
    }
}
