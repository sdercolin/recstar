package audio

import model.AppPreference
import model.AppPreference.BitDepthOption

/**
 * A common data class to define an audio format.
 */
data class AudioFormat(
    val sampleRate: Int,
    val bitDepth: Int,
    val channelCount: Int,
    val signed: Boolean,
    val littleEndian: Boolean,
    val floating: Boolean,
)

fun AppPreference.getAudioFormat(): AudioFormat =
    AudioFormat(
        sampleRate = sampleRate.value,
        bitDepth = when (bitDepth) {
            BitDepthOption.BitDepth16 -> 16
            BitDepthOption.BitDepth24 -> 24
            BitDepthOption.BitDepth32 -> 32
            BitDepthOption.BitDepth32Float -> 32
        },
        channelCount = 1,
        signed = true,
        littleEndian = true,
        floating = bitDepth == BitDepthOption.BitDepth32Float,
    )

expect fun BitDepthOption.isSupported(): Boolean
