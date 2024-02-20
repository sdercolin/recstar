package audio

import model.AppPreference

actual suspend fun AppPreference.BitDepthOption.isSupported(appPreference: AppPreference): Boolean =
    when (this) {
        AppPreference.BitDepthOption.BitDepth16 -> true
        AppPreference.BitDepthOption.BitDepth32Float -> true
        else -> false
    }
