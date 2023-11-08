package util

actual object Platform {
    actual val target: PlatformTarget = PlatformTarget.Desktop
    actual val os: String get() = osInfo
}
