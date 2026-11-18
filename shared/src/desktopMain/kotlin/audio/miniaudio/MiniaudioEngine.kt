package audio.miniaudio

import audio.AudioDeviceInfo
import audio.AudioDeviceInfoList
import audio.AudioFormat
import audio.AudioPlayer
import audio.AudioRecorder
import audio.DesktopAudioEngine
import audio.buildAudioDeviceInfoList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.AppPreference.AudioBackend
import repository.AppPreferenceRepository
import ui.common.ErrorNotifier
import ui.model.AppContext

internal class MiniaudioEngine(private val backend: AudioBackend) : DesktopAudioEngine {
    // miniaudio converts sample formats and rates internally, so every device accepts every app format.
    override suspend fun getInputDeviceInfos(
        desiredDeviceName: String?,
        audioFormat: AudioFormat,
    ): AudioDeviceInfoList? = getDeviceInfos(desiredDeviceName, isCapture = true)

    override suspend fun getOutputDeviceInfos(
        desiredDeviceName: String?,
        audioFormat: AudioFormat,
    ): AudioDeviceInfoList? = getDeviceInfos(desiredDeviceName, isCapture = false)

    private suspend fun getDeviceInfos(
        desiredDeviceName: String?,
        isCapture: Boolean,
    ): AudioDeviceInfoList? =
        withContext(Dispatchers.IO) {
            val devices = MiniaudioContext.get(backend).getDevices(isCapture).map {
                AudioDeviceInfo(name = it.name, displayName = it.name, isDefault = it.isDefault)
            }
            buildAudioDeviceInfoList(desiredDeviceName, devices)
        }

    override fun createRecorder(
        listener: AudioRecorder.Listener,
        errorNotifier: ErrorNotifier,
        appPreferenceRepository: AppPreferenceRepository,
    ): AudioRecorder = MiniaudioRecorder(backend, listener, errorNotifier, appPreferenceRepository)

    override fun createPlayer(
        listener: AudioPlayer.Listener,
        context: AppContext,
        errorNotifier: ErrorNotifier,
        appPreferenceRepository: AppPreferenceRepository,
    ): AudioPlayer = TODO("miniaudio player is not implemented yet")

    companion object {
        fun getAvailableBackends(): List<AudioBackend> {
            if (!NativeAudio.isAvailable) return emptyList()
            return AudioBackend.entries.filter {
                NativeAudio.ra_backend_is_available(it.nativeId) == 1 && MiniaudioContext.getOrNull(it) != null
            }
        }
    }
}
