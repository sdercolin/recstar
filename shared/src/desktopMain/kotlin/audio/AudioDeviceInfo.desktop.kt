package audio

actual suspend fun getAudioInputDeviceInfos(
    desiredDeviceName: String?,
    audioFormat: AudioFormat,
): AudioDeviceInfoList? = currentDesktopAudioEngine().getInputDeviceInfos(desiredDeviceName, audioFormat)

actual suspend fun getAudioOutputDeviceInfos(
    desiredDeviceName: String?,
    audioFormat: AudioFormat,
): AudioDeviceInfoList? = currentDesktopAudioEngine().getOutputDeviceInfos(desiredDeviceName, audioFormat)
