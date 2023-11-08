package util

import platform.UIKit.UIDevice

actual object Platform {
    actual val target: PlatformTarget = PlatformTarget.Ios
    actual val os: String = UIDevice.currentDevice.run { "$systemName $systemVersion" }
}
