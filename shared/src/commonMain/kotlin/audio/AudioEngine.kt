package audio

import model.AppPreference.AudioBackend
import model.AppPreference.AudioEngine

/**
 * Returns the audio engines that can be selected on the current platform. Empty if the platform has no choice.
 */
expect fun getAvailableAudioEngines(): List<AudioEngine>

/**
 * Returns the backends that can be selected for the given [engine], including [AudioBackend.Auto]. Empty if the
 * engine has no choice.
 */
expect fun getAvailableAudioBackends(engine: AudioEngine): List<AudioBackend>
