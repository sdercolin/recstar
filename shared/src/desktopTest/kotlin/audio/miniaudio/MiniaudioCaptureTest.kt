package audio.miniaudio

import audio.AudioFormat
import model.AppPreference.AudioBackend
import org.junit.Assume.assumeTrue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MiniaudioCaptureTest {
    private val context = MiniaudioContext.get(AudioBackend.Auto)

    @Test
    fun capturesFromDefaultDevice() {
        assumeTrue("No capture device", context.getDevices(isCapture = true).isNotEmpty())
        val format = AudioFormat(48000, 24, 1, signed = true, littleEndian = true, floating = false)
        context.openCapture(null, format, bufferMs = 1000).use { capture ->
            capture.start()
            Thread.sleep(300)
            assertTrue(capture.isRunning)
            capture.stop()
            val buffer = ByteArray(48000 * capture.bytesPerFrame)
            val frames = capture.read(buffer)
            assertTrue(frames in 9600..48000, "frames: $frames")
            assertEquals(0L, capture.droppedFrames)
            assertEquals(0, capture.read(buffer))
        }
    }

    @Test(expected = exception.UnsupportedAudioDeviceException::class)
    fun failsForUnknownDevice() {
        val format = AudioFormat(44100, 16, 1, signed = true, littleEndian = true, floating = false)
        context.openCapture("No Such Device デバイス", format, bufferMs = 1000)
    }
}
