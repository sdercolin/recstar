package audio

import androidx.compose.runtime.Composable
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

expect suspend fun getAudioInputDeviceInfos(
    desiredDeviceName: String?,
    audioFormat: AudioFormat,
): AudioDeviceInfoList?

expect suspend fun getAudioOutputDeviceInfos(
    desiredDeviceName: String?,
    audioFormat: AudioFormat,
): AudioDeviceInfoList?
