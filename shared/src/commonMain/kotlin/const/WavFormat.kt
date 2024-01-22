package const

object WavFormat {
    const val CHUNK_ID = "RIFF"
    const val FORMAT = "WAVE"
    const val SUBCHUNK_1_ID = "fmt "
    const val SUBCHUNK_1_SIZE = 16
    const val SUBCHUNK_2_ID = "data"
    const val AUDIO_FORMAT_PCM = 1.toShort()
    const val AUDIO_FORMAT_FLOAT = 3.toShort()
    const val CHANNELS = 1
    const val SAMPLE_RATE = 44100
    const val BITS_PER_SAMPLE = 16
    const val BYTE_RATE = SAMPLE_RATE * CHANNELS * BITS_PER_SAMPLE / 8
    const val HEADER_EXTRA_SIZE = 36 // 44 - 8 (the size of the first two values in the header)
}
