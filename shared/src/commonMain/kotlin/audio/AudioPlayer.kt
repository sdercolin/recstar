package audio

import io.File
import ui.model.AppContext

/**
 * An interface for playing audio.
 */
interface AudioPlayer {
    /**
     * A listener for [AudioPlayer] events.
     */
    interface Listener {
        fun onStarted()

        fun onProgress(progress: Float)

        fun onStopped()
    }

    /**
     * An asynchronous operation that starts playing the given [file].
     */
    fun play(file: File)

    /**
     * An asynchronous operation that stops the playback.
     */
    fun stop()

    /**
     * Returns `true` if the player is currently playing.
     */
    fun isPlaying(): Boolean

    /**
     * Disposes of the resources used by the player.
     */
    fun dispose()
}

expect class AudioPlayerProvider(listener: AudioPlayer.Listener, context: AppContext) {
    fun get(): AudioPlayer
}