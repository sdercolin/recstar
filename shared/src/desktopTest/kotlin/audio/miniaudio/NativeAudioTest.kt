package audio.miniaudio

import model.AppPreference.AudioBackend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NativeAudioTest {
    @Test
    fun loadsBundledLibrary() {
        assertTrue(NativeAudio.isAvailable)
        assertEquals("0.11.25", NativeAudio.ra_version())
    }

    @Test
    fun autoBackendIsAlwaysAvailable() {
        assertTrue(AudioBackend.Auto in MiniaudioEngine.getAvailableBackends())
    }

    @Test
    fun enumeratesDevicesWithAutoBackend() {
        val context = MiniaudioContext.get(AudioBackend.Auto)
        context.getDevices(isCapture = true).forEach { println("capture: $it") }
        context.getDevices(isCapture = false).forEach { println("playback: $it") }
    }
}
