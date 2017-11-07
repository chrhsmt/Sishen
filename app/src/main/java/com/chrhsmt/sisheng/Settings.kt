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
    // 対数得点計算の底
    var baseLogarithmForPoint: Int = 5
    // ポイント成功閾値
    var pointSuccessThreshold: Int = 80

    // Raspberry pi
    var raspberrypiHost: String = ""
    var raspberrypiPath: String = ""

    // Debug Mode
    val DEBUG_MODE = true

    // Emulator Mode (音声再生、録音が動作しない)
    val EMULATOR_MODE = false

    // 萌え系フォントへの変更要否
    val USE_MOE_FONT = true

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
            raspberrypiHost = context.getString(R.string.default_pi_host) + ":" + context.getString(R.string.default_pi_port)
        }
        if (force || Settings.raspberrypiPath == "") {
            raspberrypiPath = context.getString(R.string.default_pi_path)
        }
    }
}
