package audio

import const.WavFormat

typealias JavaAudioFormat = javax.sound.sampled.AudioFormat

actual fun getDefaultAudioFormat(): AudioFormat =
    AudioFormat(
        sampleRate = WavFormat.SAMPLE_RATE,
        bitDepth = WavFormat.BITS_PER_SAMPLE,
        channelCount = WavFormat.CHANNELS,
        signed = true,
        littleEndian = false,
    )

fun AudioFormat.toJavaAudioFormat(): JavaAudioFormat =
    JavaAudioFormat(
        sampleRate.toFloat(),
        bitDepth,
        channelCount,
        signed,
        littleEndian,
    )

fun JavaAudioFormat.toAudioFormat(): AudioFormat =
    AudioFormat(
        sampleRate = sampleRate.toInt(),
        bitDepth = sampleSizeInBits,
        channelCount = channels,
        signed = encoding == javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED,
        littleEndian = isBigEndian,
    )
