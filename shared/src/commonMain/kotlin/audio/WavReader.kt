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

        require(chunkId == WavFormat.CHUNK_ID) {
            "Invalid WAV file: chunkId: $chunkId"
        }
        require(format == WavFormat.FORMAT) {
            "Invalid WAV file: format: $format"
        }
        var subchunk1Id = source.readString(4)
        while (subchunk1Id != WavFormat.SUBCHUNK_1_ID) {
            source.skip(source.readIntLe().toLong())
            subchunk1Id = source.readString(4)
        }
        val subchunk1Size = source.readIntLe()
        val audioFormat = source.readShortLe()
        val numChannels = source.readShortLe()
        val sampleRate = source.readIntLe()
        val byteRate = source.readIntLe()
        val blockAlign = source.readShortLe()
        val bitsPerSample = source.readShortLe()

        require(audioFormat == WavFormat.AUDIO_FORMAT) {
            "Invalid WAV file: audioFormat: $audioFormat"
        }
        require(bitsPerSample == 16.toShort()) {
            "Only 16-bit WAV files are supported"
        }

        var subchunk2Id = source.readString(4)
        while (subchunk2Id != WavFormat.SUBCHUNK_2_ID) {
            source.skip(source.readIntLe().toLong())
            subchunk2Id = source.readString(4)
        }
        val subchunk2Size = source.readIntLe()

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
