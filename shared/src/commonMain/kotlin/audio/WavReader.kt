package audio

import const.WavFormat
import io.File
import kotlinx.io.Source
import kotlinx.io.readFloatLe
import kotlinx.io.readIntLe
import kotlinx.io.readShortLe
import kotlinx.io.readString
import util.Log

@Suppress("UNUSED_VARIABLE")
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

        val expectedByteRate = sampleRate * numChannels * bitsPerSample / 8
        val expectedBlockAlign = numChannels * bitsPerSample / 8

        if (expectedByteRate != byteRate) {
            Log.w("Unexpected byte rate: $byteRate, expected: $expectedByteRate")
        }
        if (expectedBlockAlign != blockAlign.toInt()) {
            Log.w("Unexpected block align: $blockAlign, expected: $expectedBlockAlign")
        }

        val supportedSampleRate = listOf(44100, 48000, 96000)
        require(sampleRate in supportedSampleRate) {
            "Unsupported sample rate: $sampleRate"
        }

        val supportedBitDepths = when (audioFormat) {
            WavFormat.AUDIO_FORMAT_PCM -> listOf(8, 16, 24, 32)
            WavFormat.AUDIO_FORMAT_FLOAT -> listOf(32)
            else -> throw IllegalArgumentException("Unsupported audio format: $audioFormat")
        }

        require(bitsPerSample.toInt() in supportedBitDepths) {
            "Unsupported bit depth: $bitsPerSample"
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
                val value = when (audioFormat) {
                    WavFormat.AUDIO_FORMAT_PCM -> readPcmSample(source, bitsPerSample)
                    WavFormat.AUDIO_FORMAT_FLOAT -> readFloatingSample(source, bitsPerSample)
                    else -> throw IllegalArgumentException("Unsupported audio format: $audioFormat")
                }
                channelBuffer[i] = value
                read += 2
            }
            data.add(channelBuffer.average().toFloat())
        }

        source.close()
        return data.toFloatArray()
    }

    private fun readPcmSample(
        source: Source,
        bitsPerSample: Short,
    ): Float =
        when (bitsPerSample.toInt()) {
            8 -> source.readByte().toFloat() / Byte.MAX_VALUE
            16 -> source.readShortLe().toFloat() / Short.MAX_VALUE
            24 -> source.read3BytesLe().toFloat() / Int.MAX_VALUE
            32 -> source.readIntLe().toFloat() / Int.MAX_VALUE
            else -> throw IllegalArgumentException("Unsupported bit depth: $bitsPerSample")
        }

    private fun readFloatingSample(
        source: Source,
        bitsPerSample: Short,
    ): Float {
        if (bitsPerSample.toInt() != 32) {
            throw IllegalArgumentException("Unsupported bit depth: $bitsPerSample")
        }
        return source.readFloatLe()
    }

    private fun Source.read3BytesLe(): Int {
        val byte1 = readByte().toInt()
        val byte2 = readByte().toInt()
        val byte3 = readByte().toInt()
        return byte1 or (byte2 shl 8) or (byte3 shl 16)
    }
}
