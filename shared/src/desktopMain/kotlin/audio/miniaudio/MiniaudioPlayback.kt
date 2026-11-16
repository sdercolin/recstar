package audio.miniaudio

import com.sun.jna.Pointer

/**
 * An open miniaudio playback device with at most one loaded sound. Thread-safe; calls after [close] are ignored.
 */
internal class MiniaudioPlayback(private val pointer: Pointer) : AutoCloseable {
    private var closed = false

    @Synchronized
    fun load(path: String) {
        if (closed) return
        check(NativeAudio.ra_player_load(pointer, path.toCString()) == NativeAudio.RA_OK) {
            "Failed to load audio file: $path"
        }
    }

    @Synchronized
    fun play(startMs: Long) {
        if (closed) return
        check(NativeAudio.ra_player_play(pointer, startMs.toDouble()) == NativeAudio.RA_OK) {
            "Failed to start playback"
        }
    }

    @Synchronized
    fun stop() {
        if (closed) return
        NativeAudio.ra_player_stop(pointer)
    }

    @Synchronized
    fun setVolume(volume: Float) {
        if (closed) return
        NativeAudio.ra_player_set_volume(pointer, volume)
    }

    val isPlaying: Boolean
        @Synchronized get() = !closed && NativeAudio.ra_player_is_playing(pointer) == 1

    /**
     * Returns the playback position as a fraction of the sound's length.
     */
    val progress: Float
        @Synchronized get() {
            if (closed) return 0f
            val length = NativeAudio.ra_player_length_ms(pointer)
            if (length <= 0.0) return 0f
            return (NativeAudio.ra_player_position_ms(pointer) / length).toFloat().coerceIn(0f, 1f)
        }

    @Synchronized
    override fun close() {
        if (closed) return
        closed = true
        NativeAudio.ra_player_close(pointer)
    }
}
