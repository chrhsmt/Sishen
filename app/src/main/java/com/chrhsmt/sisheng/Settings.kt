package com.chrhsmt.sisheng

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
}
