package audio

import audio.javasound.JavaSoundEngine
import model.AppPreference
import model.AppPreference.AudioBackend
import model.AppPreference.AudioEngine
import repository.AppPreferenceRepository
import ui.common.ErrorNotifier
import ui.model.AppContext

internal interface DesktopAudioEngine {
    suspend fun getInputDeviceInfos(
        desiredDeviceName: String?,
        audioFormat: AudioFormat,
    ): AudioDeviceInfoList?

    suspend fun getOutputDeviceInfos(
        desiredDeviceName: String?,
        audioFormat: AudioFormat,
    ): AudioDeviceInfoList?

    fun createRecorder(
        listener: AudioRecorder.Listener,
        errorNotifier: ErrorNotifier,
        appPreferenceRepository: AppPreferenceRepository,
    ): AudioRecorder

    fun createPlayer(
        listener: AudioPlayer.Listener,
        context: AppContext,
        errorNotifier: ErrorNotifier,
        appPreferenceRepository: AppPreferenceRepository,
    ): AudioPlayer
}

internal fun AppPreference.desktopAudioEngineKey(): DesktopAudioEngineKey {
    val engine = audioEngine.takeIf { it in getAvailableAudioEngines() } ?: AudioEngine.JavaSound
    val backend = audioBackend.takeIf { it in getAvailableAudioBackends(engine) } ?: AudioBackend.Auto
    return DesktopAudioEngineKey(engine, backend)
}

internal data class DesktopAudioEngineKey(
    val engine: AudioEngine,
    val backend: AudioBackend,
)

internal fun DesktopAudioEngineKey.resolve(): DesktopAudioEngine =
    when (engine) {
        AudioEngine.JavaSound -> JavaSoundEngine
        AudioEngine.Miniaudio -> error("miniaudio engine is not available")
    }

internal fun AppPreference.desktopAudioEngine(): DesktopAudioEngine = desktopAudioEngineKey().resolve()
