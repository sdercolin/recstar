package audio

import androidx.compose.runtime.Composable
import model.AppPreference
import ui.string.*

/**
 * Audio device info.
 *
 * @property name The name of the device. Should be unique.
 * @property displayName The display name of the device.
 * @property isDefault Whether the device is the default device.
 * @property notFound Whether the device is not found.
 */
data class AudioDeviceInfo(
    val name: String,
    val displayName: String,
    val isDefault: Boolean,
    val notFound: Boolean = false,
) : LocalizedText {
    override val textKey: Strings
        get() = error("Not accessible")

    @Composable
    override fun getText(): String =
        if (notFound) string(Strings.PreferenceDeviceNameNotFoundTemplate, displayName) else displayName
}

/**
 * Audio device info list.
 */
data class AudioDeviceInfoList(
    val deviceInfos: List<AudioDeviceInfo>,
    val selectedDeviceInfo: AudioDeviceInfo,
)

/**
 * Returns the input devices available to the audio engine selected in [appPreference], with the desired one selected.
 */
expect suspend fun getAudioInputDeviceInfos(appPreference: AppPreference): AudioDeviceInfoList?

/**
 * Returns the output devices available to the audio engine selected in [appPreference], with the desired one selected.
 */
expect suspend fun getAudioOutputDeviceInfos(appPreference: AppPreference): AudioDeviceInfoList?
