package audio.javasound

import audio.AudioFormat
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
