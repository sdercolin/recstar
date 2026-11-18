package audio

import model.AppPreference.AudioBackend
import model.AppPreference.AudioEngine

actual fun getAvailableAudioEngines(): List<AudioEngine> = listOf(AudioEngine.JavaSound)

actual fun getAvailableAudioBackends(engine: AudioEngine): List<AudioBackend> = emptyList()
