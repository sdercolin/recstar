package util

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase

expect object Platform {
    val target: PlatformTarget
    val os: String
}

enum class PlatformTarget {
    Android,
    Desktop,
    Ios,
    ;

    override fun toString(): String =
        when (this) {
            Android -> "Android"
            Desktop -> "Desktop"
            Ios -> "iOS"
        }
}

val isAndroid: Boolean = Platform.target == PlatformTarget.Android
val isDesktop: Boolean = Platform.target == PlatformTarget.Desktop
val isIos: Boolean = Platform.target == PlatformTarget.Ios
val isMobile: Boolean = isAndroid || isIos
val useIosStyle: Boolean = isIos || (isDesktop && Platform.os.toLowerCase(Locale.current).contains("mac"))
