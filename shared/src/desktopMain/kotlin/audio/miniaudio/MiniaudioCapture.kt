package audio.miniaudio

import audio.AudioFormat
import com.sun.jna.Pointer

/**
 * An open miniaudio capture device. Not thread-safe; use it from one coroutine at a time.
 */
internal class MiniaudioCapture(
    private val pointer: Pointer,
    val format: AudioFormat,
) : AutoCloseable {
    private var closed = false

    val bytesPerFrame: Int = format.bitDepth / 8 * format.channelCount

    val isRunning: Boolean get() = NativeAudio.ra_capture_is_running(pointer) == 1

    val droppedFrames: Long get() = NativeAudio.ra_capture_dropped_frames(pointer)

    fun start() {
        check(NativeAudio.ra_capture_start(pointer) == NativeAudio.RA_OK) { "Failed to start capture device" }
    }

    fun stop() {
        NativeAudio.ra_capture_stop(pointer)
    }

    /**
     * Copies up to `buffer.size / bytesPerFrame` frames into [buffer] without blocking and returns the frame count.
     */
    fun read(buffer: ByteArray): Int = NativeAudio.ra_capture_read(pointer, buffer, buffer.size / bytesPerFrame)

    override fun close() {
        if (closed) return
        closed = true
        NativeAudio.ra_capture_close(pointer)
    }
}
