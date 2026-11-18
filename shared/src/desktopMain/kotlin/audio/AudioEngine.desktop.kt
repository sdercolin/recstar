package audio

import audio.miniaudio.MiniaudioEngine
import audio.miniaudio.NativeAudio
import model.AppPreference.AudioBackend
import model.AppPreference.AudioEngine

actual fun getAvailableAudioEngines(): List<AudioEngine> =
    if (NativeAudio.isAvailable) {
        listOf(AudioEngine.JavaSound, AudioEngine.Miniaudio)
    } else {
        listOf(AudioEngine.JavaSound)
    }

actual fun getAvailableAudioBackends(engine: AudioEngine): List<AudioBackend> =
    when (engine) {
        AudioEngine.JavaSound -> emptyList()
        AudioEngine.Miniaudio -> MiniaudioEngine.getAvailableBackends()
    }
