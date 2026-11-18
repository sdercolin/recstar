package audio

import kotlin.test.Test
import kotlin.test.assertEquals

class PcmDecoderTest {
    private fun format(
        bitDepth: Int,
        channels: Int = 1,
        floating: Boolean = false,
    ) = AudioFormat(44100, bitDepth, channels, signed = true, littleEndian = true, floating = floating)

    private fun decode(
        format: AudioFormat,
        bytes: ByteArray,
        frames: Int,
    ): FloatArray = FloatArray(frames).also { PcmDecoder(format).decodeFirstChannel(bytes, frames, it, 0) }

    @Test
    fun decodes16Bit() {
        val bytes = byteArrayOf(0xFF.toByte(), 0x7F, 0x01, 0x80.toByte())
        val result = decode(format(16), bytes, 2)
        assertEquals(1f, result[0])
        assertEquals(-1f, result[1])
    }

    @Test
    fun decodes24BitWithHighLowBytes() {
        val bytes = byteArrayOf(0x80.toByte(), 0x80.toByte(), 0x00, 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())
        val result = decode(format(24), bytes, 2)
        assertEquals(0x8080 / 8388607f, result[0])
        assertEquals(-1 / 8388607f, result[1])
    }

    @Test
    fun decodesFloat() {
        val bits = 0.25f.toRawBits()
        val bytes = byteArrayOf(bits.toByte(), (bits shr 8).toByte(), (bits shr 16).toByte(), (bits shr 24).toByte())
        assertEquals(0.25f, decode(format(32, floating = true), bytes, 1)[0])
    }

    @Test
    fun takesFirstChannelOfStereo() {
        val bytes = byteArrayOf(0xFF.toByte(), 0x7F, 0x00, 0x00, 0x00, 0x00, 0xFF.toByte(), 0x7F)
        val result = decode(format(16, channels = 2), bytes, 2)
        assertEquals(1f, result[0])
        assertEquals(0f, result[1])
    }
}
