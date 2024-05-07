package audio.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlin.math.log2
import kotlin.math.pow

/**
 * Data class to represent pitch data.
 *
 * @param freq A list of fundamental data.
 * @param corr A list of correlation data.
 */
@Immutable
data class PitchData(
    val freq: List<Float>,
    val corr: List<Float>,
) {
    companion object {
        val EMPTY = PitchData(listOf(), listOf())
    }
}

/**
 * Convert wave data to pitch data.
 */
fun WavData.toFundamental(
    funConf: FundamentalConfigs,
    sampleRate: Float,
): PitchData {
    // maybe add other algorithms in the future
    return this.toFundamentalSwipePrime(funConf, sampleRate)
}

/**
 * Convert frequency to semitone (MIDI note number) and vice versa.
 */
object Semitone {
    fun fromFrequency(freq: Float): Float {
        return 12f * (log2(freq / 440f)) + 69f
    }

    fun toFrequency(semitone: Float): Float {
        return 440f * 2f.pow((semitone - 69f) / 12f)
    }
}

/**
 * Settings for calculating fundamental frequency.
 */
@Serializable
@Immutable
data class FundamentalConfigs(
    val enabled: Boolean = DEFAULT_ENABLED,
    val semitoneResolution: Int = DEFAULT_SEMITONE_RESOLUTION,
    val centerSemitone: Int = DEFAULT_CENTER_SEMITONE,
    val radiusSemitone: Int = DEFAULT_RADIUS_SEMITONE,
    val semitoneSampleNum: Int = DEFAULT_SEMITONE_SAMPLE_NUM,
    val maxHarmonicFrequency: Float = DEFAULT_MAX_HARMONIC_FREQUENCY,
    val erbsStep: Float = DEFAULT_ERBS_STEP,
    val minDisplayCorr: Float = DEFAULT_MIN_DISPLAY_CORR,
    val maxDisplayCorr: Float = DEFAULT_MAX_DISPLAY_CORR,
) {
    fun validate() {
        require(semitoneResolution > 0) { "semitoneResolution must be positive" }
        require(radiusSemitone >= 0) { "radiusSemitone must be non-negative" }
        require(semitoneSampleNum > 0) { "semitoneSampleNum must be positive" }
        require(maxHarmonicFrequency > 0) { "maxHarmonicFrequency must be positive" }
        require(erbsStep > 0) { "erbsStep must be positive" }
        require(minDisplayCorr >= 0) { "minDisplayCorr must be non-negative" }
        require(maxDisplayCorr >= 0) { "maxDisplayCorr must be non-negative" }
        require(minDisplayCorr <= maxDisplayCorr) { "minDisplayCorr must be less than or equal to maxDisplayCorr" }
    }

    companion object {
        const val DEFAULT_ENABLED = false
        const val DEFAULT_SEMITONE_RESOLUTION = 8
        const val DEFAULT_CENTER_SEMITONE = 60
        const val DEFAULT_RADIUS_SEMITONE = 1
        const val DEFAULT_SEMITONE_SAMPLE_NUM = 8
        const val DEFAULT_MAX_HARMONIC_FREQUENCY = 5000.0f
        const val DEFAULT_ERBS_STEP = 0.1f
        const val DEFAULT_MIN_DISPLAY_CORR = 0.0f
        const val DEFAULT_MAX_DISPLAY_CORR = 0.5f
    }
}
