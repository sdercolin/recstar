package audio

import const.WavFormat
import io.File
import kotlinx.io.Source
import kotlinx.io.readFloatLe
import kotlinx.io.readIntLe
import kotlinx.io.readShortLe
import kotlinx.io.readString
import kotlinx.io.readUByte
import util.Log

@Suppress("UNUSED_VARIABLE")
class WavReader {
    /**
     * Reads the given [file] and returns the audio data as [WavData] (value in float -1~1). If the file has multiple
     * channels, the data will be merged into a single channel.
     */
    fun read(file: File): WavData {
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
        var readSize = 0
        val audioFormat = source.readShortLe()
        var resolvedAudioFormat = audioFormat
        readSize += 2
        val numChannels = source.readShortLe()
        readSize += 2
        val sampleRate = source.readIntLe()
        readSize += 4
        val byteRate = source.readIntLe()
        readSize += 4
        val blockAlign = source.readShortLe()
        readSize += 2
        val bitsPerSample = source.readShortLe()
        readSize += 2

        if (audioFormat == WavFormat.AUDIO_FORMAT_EXTENSIBLE) {
            when (val cbSize = source.readShortLe().toInt()) {
                0 -> {
                    readSize += 2
                }
                22 -> {
                    readSize += 2
                    val validBitsPerSample = source.readShortLe()
                    readSize += 2
                    val channelMask = source.readIntLe()
                    readSize += 4
                    resolvedAudioFormat = source.readShortLe()
                    source.skip(14)
                    readSize += 16
                }
                else -> throw IllegalArgumentException("Unsupported cbSize: $cbSize")
            }
        }

        val restChunk1IdSize = subchunk1Size - readSize
        if (restChunk1IdSize > 0) {
            source.skip(restChunk1IdSize.toLong())
        }

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

        val supportedBitDepths = when (resolvedAudioFormat) {
            WavFormat.AUDIO_FORMAT_PCM -> listOf(8, 16, 24, 32)
            WavFormat.AUDIO_FORMAT_FLOAT -> listOf(32)
            else -> throw IllegalArgumentException("Unsupported resolved audio format: $resolvedAudioFormat")
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

        val data = mutableListOf<FloatArray>()
        var read = 0
        val channelBuffer = FloatArray(numChannels.toInt())
        val onRead = { byteCount: Int ->
            read += byteCount
        }
        while (read < subchunk2Size) {
            for (i in 0 until numChannels) {
                val value = when (resolvedAudioFormat) {
                    WavFormat.AUDIO_FORMAT_PCM -> readPcmSample(source, bitsPerSample, onRead)
                    WavFormat.AUDIO_FORMAT_FLOAT -> readFloatingSample(source, bitsPerSample, onRead)
                    else -> throw IllegalArgumentException("Unsupported audio format: $audioFormat")
                }
                channelBuffer[i] = value
            }
            data.add(channelBuffer.toTypedArray().toFloatArray())
        }

        source.close()
        return data.toTypedArray()
    }

    private fun readPcmSample(
        source: Source,
        bitsPerSample: Short,
        onRead: (Int) -> Unit,
    ): Float =
        when (bitsPerSample.toInt()) {
            8 -> {
                onRead(1)
                (source.readUByte().toInt() - 128) / 128f
            }
            16 -> {
                onRead(2)
                source.readShortLe().toFloat() / Short.MAX_VALUE
            }
            24 -> {
                onRead(3)
                source.read3BytesLe().toFloat() / 8388607f // 2^23 - 1
            }
            32 -> {
                onRead(4)
                source.readIntLe().toFloat() / Int.MAX_VALUE
            }
            else -> throw IllegalArgumentException("Unsupported bit depth: $bitsPerSample")
        }

    private fun readFloatingSample(
        source: Source,
        bitsPerSample: Short,
        onRead: (Int) -> Unit,
    ): Float {
        if (bitsPerSample.toInt() != 32) {
            throw IllegalArgumentException("Unsupported bit depth: $bitsPerSample")
        }
        onRead(4)
        return source.readFloatLe()
    }

    private fun Source.read3BytesLe(): Int {
        val byte1 = readByte().toInt()
        val byte2 = readByte().toInt()
        val byte3 = readByte().toInt()
        return byte1 or (byte2 shl 8) or (byte3 shl 16)
    }
}
