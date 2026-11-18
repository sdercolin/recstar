package audio

import const.WavFormat
import java.io.Closeable
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Writes little-endian PCM or IEEE float samples to a WAV file. The RIFF and data sizes are patched by
 * [updateHeader] and [close], so a file interrupted before closing still holds everything up to the last update.
 */
internal class WavWriter(
    file: java.io.File,
    private val format: AudioFormat,
) : Closeable {
    private val output = RandomAccessFile(file, "rw")
    private var dataSize = 0L

    init {
        require(format.littleEndian) { "Only little-endian formats are supported" }
        require(format.floating || format.signed || format.bitDepth == 8) { "Unsigned PCM is only valid for 8 bit" }
        output.setLength(0)
        output.write(createHeader())
    }

    val bytesPerFrame: Int get() = format.bitDepth / 8 * format.channelCount

    fun write(
        buffer: ByteArray,
        offset: Int = 0,
        length: Int = buffer.size,
    ) {
        require(length % bytesPerFrame == 0) { "Length $length is not a multiple of the frame size $bytesPerFrame" }
        require(dataSize + length <= MAX_DATA_SIZE) { "WAV data cannot exceed $MAX_DATA_SIZE bytes" }
        output.write(buffer, offset, length)
        dataSize += length
    }

    fun updateHeader() {
        val position = output.filePointer
        output.seek(RIFF_SIZE_OFFSET)
        output.write(intLe((WavFormat.HEADER_EXTRA_SIZE + dataSize).toInt()))
        output.seek(DATA_SIZE_OFFSET)
        output.write(intLe(dataSize.toInt()))
        output.seek(position)
    }

    override fun close() {
        output.use { updateHeader() }
    }

    private fun createHeader(): ByteArray {
        val blockAlign = bytesPerFrame
        return ByteBuffer.allocate(HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN)
            .put(WavFormat.CHUNK_ID.encodeToByteArray())
            .putInt(WavFormat.HEADER_EXTRA_SIZE)
            .put(WavFormat.FORMAT.encodeToByteArray())
            .put(WavFormat.SUBCHUNK_1_ID.encodeToByteArray())
            .putInt(WavFormat.SUBCHUNK_1_SIZE)
            .putShort(if (format.floating) WavFormat.AUDIO_FORMAT_FLOAT else WavFormat.AUDIO_FORMAT_PCM)
            .putShort(format.channelCount.toShort())
            .putInt(format.sampleRate)
            .putInt(format.sampleRate * blockAlign)
            .putShort(blockAlign.toShort())
            .putShort(format.bitDepth.toShort())
            .put(WavFormat.SUBCHUNK_2_ID.encodeToByteArray())
            .putInt(0)
            .array()
    }

    private fun intLe(value: Int): ByteArray =
        ByteBuffer.allocate(
            4,
        ).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()

    private companion object {
        const val HEADER_SIZE = 44
        const val RIFF_SIZE_OFFSET = 4L
        const val DATA_SIZE_OFFSET = 40L
        const val MAX_DATA_SIZE = Int.MAX_VALUE.toLong() - WavFormat.HEADER_EXTRA_SIZE
    }
}
