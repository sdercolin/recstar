package graph

import audio.model.FundamentalConfigs
import audio.model.PitchData
import audio.model.Semitone
import audio.model.WavData
import audio.model.toFundamental
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class PitchPainter(
    private val wavPointPerPixel: Int,
    private val coroutineContext: CoroutineContext = Dispatchers.Default,
) {
    /**
     * Data class to represent pitch graph data.
     *
     * @param wavPixelLength The size of the corresponding wav data, which has been resampled.
     * @param pitch The pitch data.
     */
    class PitchGraphData(
        val wavPixelLength: Int,
        val pitch: Array<FloatArray>,
    )

    private val _pitch = MutableStateFlow(PitchGraphData(0, emptyArray()))
    val pitch: Flow<PitchGraphData> = _pitch

    suspend fun putData(
        data: WavData,
        sampleRate: Int,
        configs: FundamentalConfigs,
    ) = withContext(coroutineContext) {
        val pitchData = data.toFundamental(configs, sampleRate.toFloat())
        _pitch.value = getSampledData(pitchData, configs, data.size)
    }

    private fun getSampledData(
        pitch: PitchData,
        configs: FundamentalConfigs,
        wavDataSize: Int,
    ): PitchGraphData {
        val freqData = pitch.freq
        val minDisplaySemitone = configs.centerSemitone - configs.radiusSemitone
        val maxDisplaySemitone = configs.centerSemitone + configs.radiusSemitone
        val corrData = pitch.corr
        val minDisplayCorr = configs.minDisplayCorr
        val maxDisplayCorr = configs.maxDisplayCorr
        val semitoneToRelativeValue: (Float) -> Float = { semitone ->
            val valueInRange = semitone.coerceIn(minDisplaySemitone.toFloat(), maxDisplaySemitone.toFloat())
            (valueInRange - minDisplaySemitone) / (maxDisplaySemitone - minDisplaySemitone)
        }
        val corrToRelativeValue: (Float) -> Float = { corr ->
            val valueInRange = corr.coerceIn(minDisplayCorr, maxDisplayCorr)
            (valueInRange - minDisplayCorr) / (maxDisplayCorr - minDisplayCorr)
        }

        val sampledData = mutableListOf<FloatArray>()
        for (i in freqData.indices) {
            val x = i / freqData.size.toFloat()
            val freq = freqData[i]
            val y = semitoneToRelativeValue(Semitone.fromFrequency(freq))
            val corr = corrToRelativeValue(corrData[i])
            sampledData.add(floatArrayOf(x, y, corr))
        }

        return PitchGraphData(
            wavPixelLength = wavDataSize / wavPointPerPixel,
            sampledData.toTypedArray(),
        )
    }
}
