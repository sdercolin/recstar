package audio.miniaudio

import audio.AudioFormat
import audio.WavWriter
import model.AppPreference.AudioBackend
import org.junit.Assume.assumeTrue
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MiniaudioPlaybackTest {
    private val context = MiniaudioContext.get(AudioBackend.Auto)

    private fun writeSilence(durationMs: Int): java.io.File {
        val format = AudioFormat(44100, 16, 1, signed = true, littleEndian = true, floating = false)
        val file = java.io.File(createTempDirectory("playback-test").toFile(), "あいう.wav")
        WavWriter(file, format).use { it.write(ByteArray(44100 * durationMs / 1000 * 2)) }
        return file
    }

    @Test
    fun playsUntilEnd() {
        assumeTrue("No playback device", context.getDevices(isCapture = false).isNotEmpty())
        val file = writeSilence(durationMs = 300)
        context.openPlayback(null).use { playback ->
            playback.load(file.absolutePath)
            playback.setVolume(0f)
            playback.play(startMs = 0)
            assertTrue(playback.isPlaying)
            Thread.sleep(150)
            val progress = playback.progress
            assertTrue(progress > 0f && progress < 1f, "progress: $progress")
            Thread.sleep(400)
            assertFalse(playback.isPlaying)

            playback.play(startMs = 200)
            assertTrue(playback.isPlaying)
            Thread.sleep(30)
            assertTrue(playback.progress >= 0.6f, "progress after seek: ${playback.progress}")
            playback.stop()
            assertFalse(playback.isPlaying)
        }
    }
}
