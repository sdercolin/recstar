package audio

/**
 * A common data class to define an audio format.
 */
data class AudioFormat(
    val sampleRate: Int,
    val bitRate: Int,
    val channelCount: Int,
    val signed: Boolean,
    val littleEndian: Boolean,
)

expect fun getDefaultAudioFormat(): AudioFormat
