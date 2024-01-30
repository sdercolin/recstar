package audio

import model.AppPreference

actual fun AppPreference.BitDepthOption.isSupported(): Boolean =
    when (this) {
        AppPreference.BitDepthOption.BitDepth16 -> true
        AppPreference.BitDepthOption.BitDepth32Float -> true
        else -> false
    }
