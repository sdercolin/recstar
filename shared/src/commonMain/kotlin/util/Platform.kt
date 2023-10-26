package util

expect object Platform {
    val target: PlatformTarget
}

enum class PlatformTarget {
    Android,
    Desktop,
    Ios,
}

val isAndroid: Boolean = Platform.target == PlatformTarget.Android
val isDesktop: Boolean = Platform.target == PlatformTarget.Desktop
val isIos: Boolean = Platform.target == PlatformTarget.Ios
val isMobile: Boolean = isAndroid || isIos
