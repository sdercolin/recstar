package audio

import model.AppPreference

actual suspend fun getAudioInputDeviceInfos(appPreference: AppPreference): AudioDeviceInfoList? {
    throw NotImplementedError("Selecting audio input device is not supported on iOS")
}

actual suspend fun getAudioOutputDeviceInfos(appPreference: AppPreference): AudioDeviceInfoList? {
    throw NotImplementedError("Selecting audio output device is not supported on iOS")
}
