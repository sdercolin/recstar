@file:OptIn(BetaInteropApi::class)

package util

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSString
import platform.Foundation.create

fun String.toNSString() = NSString.create(string = this)