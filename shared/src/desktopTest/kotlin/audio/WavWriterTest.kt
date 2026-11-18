package audio

import io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WavWriterTest {
    private val dir = createTempDirectory("wav-writer-test").toFile().apply { deleteOnExit() }
    private val samples = floatArrayOf(0f, 0.5f, -0.5f, 0.25f, -0.999f, 0.0001f, -0.0001f)

    private fun format(
        bitDepth: Int,
        floating: Boolean = false,
    ) = AudioFormat(
        sampleRate = 44100,
        bitDepth = bitDepth,
        channelCount = 1,
        signed = true,
        littleEndian = true,
        floating = floating,
    )

    private fun encode(
        format: AudioFormat,
        values: FloatArray,
    ): ByteArray {
        val buffer = ByteBuffer.allocate(values.size * format.bitDepth / 8).order(ByteOrder.LITTLE_ENDIAN)
        values.forEach { value ->
            when {
                format.floating -> buffer.putFloat(value)
                format.bitDepth == 16 -> buffer.putShort((value * Short.MAX_VALUE).toInt().toShort())
                format.bitDepth == 24 -> {
                    val int = (value * 8388607f).toInt()
                    buffer.put(int.toByte()).put((int shr 8).toByte()).put((int shr 16).toByte())
                }
                format.bitDepth == 32 -> buffer.putInt((value.toDouble() * Int.MAX_VALUE).toInt())
            }
        }
        return buffer.array()
    }

    private fun roundTrip(
        format: AudioFormat,
        tolerance: Float,
    ) {
        val file = java.io.File(dir, "${format.bitDepth}-${format.floating}.wav")
        WavWriter(file, format).use { it.write(encode(format, samples)) }

        assertEquals(44L + samples.size * format.bitDepth / 8, file.length())
        val read = WavReader().read(File(file.absolutePath))
        assertEquals(samples.size, read.size)
        samples.forEachIndexed { index, expected ->
            assertEquals(expected, read[index][0], tolerance, "sample $index")
        }
    }

    @Test
    fun roundTrip16Bit() = roundTrip(format(16), 1f / Short.MAX_VALUE)

    @Test
    fun roundTrip24Bit() = roundTrip(format(24), 1f / 8388607f)

    @Test
    fun roundTrip32Bit() = roundTrip(format(32), 1e-6f)

    @Test
    fun roundTrip32BitFloat() = roundTrip(format(32, floating = true), 0f)

    @Test
    fun headerIsReadableBeforeClose() {
        val file = java.io.File(dir, "partial.wav")
        val writer = WavWriter(file, format(16))
        writer.write(encode(format(16), samples))
        writer.updateHeader()
        writer.write(encode(format(16), samples))

        val read = WavReader().read(File(file.absolutePath))
        assertEquals(samples.size, read.size)
        writer.close()
        assertEquals(samples.size * 2, WavReader().read(File(file.absolutePath)).size)
    }

    @Test
    fun writesFloatFormatTag() {
        val file = java.io.File(dir, "tag.wav")
        WavWriter(file, format(32, floating = true)).use { }
        val header = file.readBytes()
        assertContentEquals(byteArrayOf(3, 0), header.copyOfRange(20, 22))
    }

    @Test
    fun rejectsPartialFrames() {
        val file = java.io.File(dir, "partial-frame.wav")
        WavWriter(file, format(24)).use { writer ->
            assertFailsWith<IllegalArgumentException> { writer.write(ByteArray(4)) }
        }
    }
}
