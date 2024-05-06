package audio.model

import model.AppPreference

actual suspend fun AppPreference.BitDepthOption.isSupported(appPreference: AppPreference): Boolean = true
