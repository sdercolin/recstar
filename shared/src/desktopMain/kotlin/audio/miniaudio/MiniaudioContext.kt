package audio.miniaudio

import audio.AudioFormat
import com.sun.jna.Pointer
import exception.UnsupportedAudioDeviceException
import exception.UnsupportedAudioFormatException
import model.AppPreference.AudioBackend

/**
 * A miniaudio context bound to one backend. Contexts are cached for the app's lifetime, because devices opened
 * later must come from the same context that enumerated them.
 */
internal class MiniaudioContext private constructor(val pointer: Pointer) {
    data class Device(
        val name: String,
        val isDefault: Boolean,
    )

    @Synchronized
    fun getDevices(isCapture: Boolean): List<Device> {
        val count = NativeAudio.ra_context_refresh_devices(pointer, isCapture.toNative())
        if (count <= 0) return emptyList()
        val buffer = ByteArray(NAME_BUFFER_SIZE)
        return (0 until count).mapNotNull { index ->
            if (NativeAudio.ra_context_device_name(pointer, isCapture.toNative(), index, buffer, buffer.size) !=
                NativeAudio.RA_OK
            ) {
                return@mapNotNull null
            }
            Device(
                name = buffer.decodeCString(),
                isDefault = NativeAudio.ra_context_device_is_default(pointer, isCapture.toNative(), index) == 1,
            )
        }
    }

    /**
     * Opens a capture device by [deviceName], or the system default device if it is null.
     */
    @Synchronized
    fun openCapture(
        deviceName: String?,
        format: AudioFormat,
        bufferMs: Int,
    ): MiniaudioCapture {
        val error = IntArray(1)
        val capture = NativeAudio.ra_capture_open(
            pointer,
            deviceName?.toCString(),
            format.sampleRate,
            format.nativeFormat,
            format.channelCount,
            bufferMs,
            error,
        )
        return when {
            capture != null -> MiniaudioCapture(capture, format)
            error[0] == NativeAudio.RA_ERROR_NO_DEVICE -> throw UnsupportedAudioDeviceException()
            else -> throw UnsupportedAudioFormatException(format)
        }
    }

    /**
     * Opens a playback device by [deviceName], or the system default device if it is null.
     */
    @Synchronized
    fun openPlayback(deviceName: String?): MiniaudioPlayback {
        val error = IntArray(1)
        val playback = NativeAudio.ra_player_open(pointer, deviceName?.toCString(), error)
        return when {
            playback != null -> MiniaudioPlayback(playback)
            error[0] == NativeAudio.RA_ERROR_NO_DEVICE -> throw UnsupportedAudioDeviceException()
            else -> throw IllegalStateException("Failed to open audio output device")
        }
    }

    companion object {
        private const val NAME_BUFFER_SIZE = 256
        private val contexts = mutableMapOf<AudioBackend, MiniaudioContext?>()

        fun get(backend: AudioBackend): MiniaudioContext =
            getOrNull(backend) ?: throw IllegalStateException("Failed to initialize miniaudio context for $backend")

        /**
         * Returns null when the backend cannot be initialized on this machine, e.g. JACK without a running server.
         * Failures are cached as well, so a backend that becomes available later needs an app restart.
         */
        @Synchronized
        fun getOrNull(backend: AudioBackend): MiniaudioContext? {
            if (contexts.containsKey(backend)) return contexts[backend]
            if (!NativeAudio.isAvailable) return null
            val context = NativeAudio.ra_context_create(backend.nativeId)?.let { MiniaudioContext(it) }
            contexts[backend] = context
            return context
        }
    }
}

private fun ByteArray.decodeCString(): String {
    val end = indexOf(0).let { if (it < 0) size else it }
    return decodeToString(0, end)
}
