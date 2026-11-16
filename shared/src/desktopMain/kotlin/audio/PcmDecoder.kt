package audio

/**
 * Decodes the first channel of interleaved little-endian samples into floats, normalized the same way as [WavReader].
 */
internal class PcmDecoder(private val format: AudioFormat) {
    private val bytesPerSample = format.bitDepth / 8
    private val bytesPerFrame = bytesPerSample * format.channelCount

    fun decodeFirstChannel(
        bytes: ByteArray,
        frameCount: Int,
        output: FloatArray,
        outputOffset: Int,
    ) {
        for (frame in 0 until frameCount) {
            output[outputOffset + frame] = decodeSample(bytes, frame * bytesPerFrame)
        }
    }

    private fun decodeSample(
        bytes: ByteArray,
        offset: Int,
    ): Float {
        fun byteAt(index: Int) = bytes[offset + index].toInt() and 0xFF
        return when {
            format.floating -> Float.fromBits(
                byteAt(0) or (byteAt(1) shl 8) or (byteAt(2) shl 16) or (byteAt(3) shl 24),
            )
            bytesPerSample == 2 -> (byteAt(0) or (bytes[offset + 1].toInt() shl 8)).toFloat() / Short.MAX_VALUE
            bytesPerSample == 3 ->
                (byteAt(0) or (byteAt(1) shl 8) or (bytes[offset + 2].toInt() shl 16)).toFloat() / 8388607f
            bytesPerSample == 4 ->
                (byteAt(0) or (byteAt(1) shl 8) or (byteAt(2) shl 16) or (byteAt(3) shl 24)).toFloat() / Int.MAX_VALUE
            else -> throw IllegalArgumentException("Unsupported bit depth: ${format.bitDepth}")
        }
    }
}
