package audio

import const.WavFormat
import io.File
import kotlinx.io.readIntLe
import kotlinx.io.readShortLe
import kotlinx.io.readString

class WavReader {
    /**
     * Reads the given [file] and returns the audio data as a [FloatArray] (-1~1). If the file has multiple channels,
     * the data will be merged into a single channel.
     */
    fun read(file: File): FloatArray {
        val source = file.source()
        val chunkId = source.readString(4)
        val chunkSize = source.readIntLe()
        val format = source.readString(4)
        val subchunk1Id = source.readString(4)
        val subchunk1Size = source.readIntLe()
        val audioFormat = source.readShortLe()
        val numChannels = source.readShortLe()
        val sampleRate = source.readIntLe()
        val byteRate = source.readIntLe()
        val blockAlign = source.readShortLe()
        val bitsPerSample = source.readShortLe()
        val subchunk2Id = source.readString(4)
        val subchunk2Size = source.readIntLe()
        if (chunkId != WavFormat.CHUNK_ID ||
            format != WavFormat.FORMAT ||
            subchunk1Id != WavFormat.SUBCHUNK_1_ID ||
            audioFormat != WavFormat.AUDIO_FORMAT ||
            subchunk2Id != WavFormat.SUBCHUNK_2_ID
        ) {
            throw IllegalArgumentException("Invalid WAV file")
        }
        if (bitsPerSample != 16.toShort()) {
            throw IllegalArgumentException("Only 16-bit WAV files are supported")
        }

        val data = mutableListOf<Float>()
        var read = 0
        val channelBuffer = FloatArray(numChannels.toInt())
        while (read < subchunk2Size) {
            for (i in 0 until numChannels) {
                val value = source.readShortLe().toFloat() / Short.MAX_VALUE
                channelBuffer[i] = value
                read += 2
            }
            data.add(channelBuffer.average().toFloat())
        }

        source.close()
        return data.toFloatArray()
    }
}
