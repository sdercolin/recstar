package audio.miniaudio

import audio.AudioFormat
import com.sun.jna.Native
import com.sun.jna.Pointer
import exception.UnsupportedAudioFormatException
import model.AppPreference.AudioBackend
import util.Log

/**
 * JNA direct mapping of `shared/native/src/recstar_audio.h`.
 */
@Suppress("FunctionName")
internal object NativeAudio {
    const val LIBRARY_NAME = "recstar_audio"

    const val RA_OK = 0
    const val RA_ERROR_NO_DEVICE = -2
    const val RA_ERROR_UNSUPPORTED_FORMAT = -4

    const val RA_FORMAT_S16 = 1
    const val RA_FORMAT_S24 = 2
    const val RA_FORMAT_S32 = 3
    const val RA_FORMAT_F32 = 4

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

    @JvmStatic
    external fun ra_capture_open(
        context: Pointer,
        deviceName: ByteArray?,
        sampleRate: Int,
        format: Int,
        channels: Int,
        bufferMs: Int,
        errorOut: IntArray,
    ): Pointer?

    @JvmStatic
    external fun ra_capture_start(capture: Pointer): Int

    @JvmStatic
    external fun ra_capture_stop(capture: Pointer): Int

    @JvmStatic
    external fun ra_capture_is_running(capture: Pointer): Int

    @JvmStatic
    external fun ra_capture_read(
        capture: Pointer,
        out: ByteArray,
        maxFrames: Int,
    ): Int

    @JvmStatic
    external fun ra_capture_dropped_frames(capture: Pointer): Long

    @JvmStatic
    external fun ra_capture_close(capture: Pointer)

    @JvmStatic
    external fun ra_player_open(
        context: Pointer,
        deviceName: ByteArray?,
        errorOut: IntArray,
    ): Pointer?

    @JvmStatic
    external fun ra_player_load(
        player: Pointer,
        path: ByteArray,
    ): Int

    @JvmStatic
    external fun ra_player_play(
        player: Pointer,
        startMs: Double,
    ): Int

    @JvmStatic
    external fun ra_player_stop(player: Pointer): Int

    @JvmStatic
    external fun ra_player_set_volume(
        player: Pointer,
        volume: Float,
    )

    @JvmStatic
    external fun ra_player_is_playing(player: Pointer): Int

    @JvmStatic
    external fun ra_player_position_ms(player: Pointer): Double

    @JvmStatic
    external fun ra_player_length_ms(player: Pointer): Double

    @JvmStatic
    external fun ra_player_close(player: Pointer)
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

/**
 * Encodes a string as a null-terminated UTF-8 C string. JNA's own String mapping uses the platform charset, which is
 * not UTF-8 on every Windows locale.
 */
internal fun String.toCString(): ByteArray = encodeToByteArray() + 0

internal val AudioFormat.nativeFormat: Int
    get() = when {
        floating && bitDepth == 32 -> NativeAudio.RA_FORMAT_F32
        floating -> throw UnsupportedAudioFormatException(this)
        bitDepth == 16 -> NativeAudio.RA_FORMAT_S16
        bitDepth == 24 -> NativeAudio.RA_FORMAT_S24
        bitDepth == 32 -> NativeAudio.RA_FORMAT_S32
        else -> throw UnsupportedAudioFormatException(this)
    }
