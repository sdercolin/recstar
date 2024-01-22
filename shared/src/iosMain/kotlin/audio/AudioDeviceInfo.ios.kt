package audio

actual suspend fun getAudioInputDeviceInfos(
    desiredDeviceName: String?,
    audioFormat: AudioFormat,
): AudioDeviceInfoList {
    throw NotImplementedError("Selecting audio input device is not supported on iOS")
}
