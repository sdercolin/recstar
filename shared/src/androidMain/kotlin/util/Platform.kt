package util

import android.os.Build

actual object Platform {
    actual val target: PlatformTarget = PlatformTarget.Android
    actual val os: String = "Android ${Build.VERSION.RELEASE}"
}
