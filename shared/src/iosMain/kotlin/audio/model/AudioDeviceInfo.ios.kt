package audio.model

actual suspend fun getAudioInputDeviceInfos(
    desiredDeviceName: String?,
    audioFormat: AudioFormat,
): AudioDeviceInfoList? {
    throw NotImplementedError("Selecting audio input device is not supported on iOS")
}

actual suspend fun getAudioOutputDeviceInfos(
    desiredDeviceName: String?,
    audioFormat: AudioFormat,
): AudioDeviceInfoList? {
    throw NotImplementedError("Selecting audio output device is not supported on iOS")
}
