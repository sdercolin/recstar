package audio.javasound

import audio.AudioDeviceInfoList
import audio.AudioFormat
import audio.AudioPlayer
import audio.AudioRecorder
import audio.DesktopAudioEngine
import repository.AppPreferenceRepository
import ui.common.ErrorNotifier
import ui.model.AppContext

internal object JavaSoundEngine : DesktopAudioEngine {
    override suspend fun getInputDeviceInfos(
        desiredDeviceName: String?,
        audioFormat: AudioFormat,
    ): AudioDeviceInfoList? = getJavaSoundInputDeviceInfos(desiredDeviceName, audioFormat)

    override suspend fun getOutputDeviceInfos(
        desiredDeviceName: String?,
        audioFormat: AudioFormat,
    ): AudioDeviceInfoList? = getJavaSoundOutputDeviceInfos(desiredDeviceName, audioFormat)

    override fun createRecorder(
        listener: AudioRecorder.Listener,
        errorNotifier: ErrorNotifier,
        appPreferenceRepository: AppPreferenceRepository,
    ): AudioRecorder = JavaSoundRecorder(listener, errorNotifier, appPreferenceRepository)

    override fun createPlayer(
        listener: AudioPlayer.Listener,
        context: AppContext,
        errorNotifier: ErrorNotifier,
        appPreferenceRepository: AppPreferenceRepository,
    ): AudioPlayer = JavaSoundPlayer(listener, context, errorNotifier, appPreferenceRepository)
}
