package audio

import io.File
import repository.AppPreferenceRepository

/**
 * An [AudioPlayer] that delegates to the engine selected in the preferences. The engine is switched only when
 * a new file starts playing, so a running playback is never interrupted.
 */
internal class EngineSwitchingPlayer(
    private val appPreferenceRepository: AppPreferenceRepository,
    private val factory: (DesktopAudioEngine) -> AudioPlayer,
) : AudioPlayer {
    private var key = appPreferenceRepository.value.desktopAudioEngineKey()
    private var delegate = factory(key.resolve())
    private var volume: Float? = null

    override fun play(
        file: File,
        positionMs: Long,
    ) {
        val newKey = appPreferenceRepository.value.desktopAudioEngineKey()
        if (newKey != key && !delegate.isPlaying()) {
            delegate.dispose()
            key = newKey
            delegate = factory(newKey.resolve())
            volume?.let { delegate.setVolume(it) }
        }
        delegate.play(file, positionMs)
    }

    override fun seekAndPlay(positionMs: Long) = delegate.seekAndPlay(positionMs)

    override fun stop() = delegate.stop()

    override fun isPlaying(): Boolean = delegate.isPlaying()

    override fun setVolume(volume: Float) {
        this.volume = volume
        delegate.setVolume(volume)
    }

    override fun dispose() = delegate.dispose()
}
