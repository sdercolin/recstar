package audio

import audio.miniaudio.MiniaudioEngine
import model.AppPreference.AudioBackend
import model.AppPreference.AudioEngine

actual fun getAvailableAudioEngines(): List<AudioEngine> = listOf(AudioEngine.JavaSound)

actual fun getAvailableAudioBackends(engine: AudioEngine): List<AudioBackend> =
    when (engine) {
        AudioEngine.JavaSound -> emptyList()
        AudioEngine.Miniaudio -> MiniaudioEngine.getAvailableBackends()
    }
