package audio

import model.AppPreference

actual suspend fun AppPreference.BitDepthOption.isSupported(appPreference: AppPreference): Boolean =
    getAudioInputDeviceInfos(null, appPreference.copy(bitDepth = this).getAudioFormat()) != null
