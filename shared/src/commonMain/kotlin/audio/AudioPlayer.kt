package audio

import io.File
import repository.AppPreferenceRepository
import ui.common.ErrorNotifier
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
    fun play(
        file: File,
        positionMs: Long = 0,
    )

    /**
     * An asynchronous operation that seeks to the given [positionMs] and starts playing.
     */
    fun seekAndPlay(positionMs: Long)

    /**
     * An asynchronous operation that stops the playback.
     */
    fun stop()

    /**
     * Returns `true` if the player is currently playing.
     */
    fun isPlaying(): Boolean

    /**
     * Sets the playback volume. [volume] should be in the range [0.0, 1.0].
     */
    fun setVolume(volume: Float)

    /**
     * Disposes of the resources used by the player.
     */
    fun dispose()
}

expect class AudioPlayerProvider(
    listener: AudioPlayer.Listener,
    context: AppContext,
    errorNotifier: ErrorNotifier,
    appPreferenceRepository: AppPreferenceRepository,
) {
    fun get(): AudioPlayer
}
