package audio.miniaudio

import com.sun.jna.Native
import com.sun.jna.Pointer
import model.AppPreference.AudioBackend
import util.Log

/**
 * JNA direct mapping of `shared/native/src/recstar_audio.h`.
 */
@Suppress("FunctionName")
internal object NativeAudio {
    const val LIBRARY_NAME = "recstar_audio"

    const val RA_OK = 0

    val isAvailable: Boolean by lazy {
        runCatching {
            Native.register(NativeAudio::class.java, LIBRARY_NAME)
            Log.i("NativeAudio: loaded miniaudio ${ra_version()}")
        }.onFailure {
            Log.e("NativeAudio: failed to load $LIBRARY_NAME")
            Log.e(it)
        }.isSuccess
    }

    @JvmStatic
    external fun ra_version(): String

    @JvmStatic
    external fun ra_backend_is_available(backend: Int): Int

    @JvmStatic
    external fun ra_context_create(backend: Int): Pointer?

    @JvmStatic
    external fun ra_context_destroy(context: Pointer)

    @JvmStatic
    external fun ra_context_refresh_devices(
        context: Pointer,
        isCapture: Int,
    ): Int

    @JvmStatic
    external fun ra_context_device_name(
        context: Pointer,
        isCapture: Int,
        index: Int,
        out: ByteArray,
        outSize: Int,
    ): Int

    @JvmStatic
    external fun ra_context_device_is_default(
        context: Pointer,
        isCapture: Int,
        index: Int,
    ): Int
}

internal val AudioBackend.nativeId: Int
    get() = when (this) {
        AudioBackend.Auto -> 0
        AudioBackend.Wasapi -> 1
        AudioBackend.DirectSound -> 2
        AudioBackend.WinMM -> 3
        AudioBackend.CoreAudio -> 4
        AudioBackend.PulseAudio -> 5
        AudioBackend.Alsa -> 6
        AudioBackend.Jack -> 7
    }

internal fun Boolean.toNative(): Int = if (this) 1 else 0
