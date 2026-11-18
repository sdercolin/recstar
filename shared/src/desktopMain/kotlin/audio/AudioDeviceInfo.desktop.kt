package audio

import model.AppPreference

actual suspend fun getAudioInputDeviceInfos(appPreference: AppPreference): AudioDeviceInfoList? =
    appPreference.desktopAudioEngine().getInputDeviceInfos(
        appPreference.desiredInputName,
        appPreference.getAudioFormat(),
    )

actual suspend fun getAudioOutputDeviceInfos(appPreference: AppPreference): AudioDeviceInfoList? =
    appPreference.desktopAudioEngine().getOutputDeviceInfos(
        appPreference.desiredOutputName,
        appPreference.getAudioFormat(),
    )
