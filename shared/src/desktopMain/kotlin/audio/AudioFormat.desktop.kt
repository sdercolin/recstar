package audio

import model.AppPreference
import javax.sound.sampled.AudioFormat.Encoding

typealias JavaAudioFormat = javax.sound.sampled.AudioFormat

fun AudioFormat.toJavaAudioFormat(): JavaAudioFormat =
    if (floating) {
        JavaAudioFormat(
            Encoding.PCM_FLOAT,
            sampleRate.toFloat(),
            bitDepth,
            channelCount,
            bitDepth / 8 * channelCount,
            sampleRate.toFloat() * channelCount,
            littleEndian.not(),
        )
    } else {
        JavaAudioFormat(
            sampleRate.toFloat(),
            bitDepth,
            channelCount,
            signed,
            littleEndian.not(),
        )
    }

actual fun AppPreference.BitDepthOption.isSupported(): Boolean =
    when (this) {
        AppPreference.BitDepthOption.BitDepth16 -> true
        else -> false
    }
