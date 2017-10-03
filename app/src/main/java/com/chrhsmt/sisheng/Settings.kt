package com.chrhsmt.sisheng

import be.tarsos.dsp.pitch.PitchProcessor

/**
 * Created by chihiro on 2017/09/26.
 */
object Settings {
    var algorithm: PitchProcessor.PitchEstimationAlgorithm = PitchProcessor.PitchEstimationAlgorithm.YIN
    var samplingRate: Int? = null
    var sex: String? = null
    var sampleAudioFileName: String? = null
}
