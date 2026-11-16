package audio

import audio.javasound.JavaSoundEngine
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

internal fun currentDesktopAudioEngine(): DesktopAudioEngine = JavaSoundEngine
