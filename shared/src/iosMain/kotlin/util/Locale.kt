package util

import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

actual val Locale: String by lazy {
    val locale = NSLocale.currentLocale
    "${locale.languageCode}-${locale.countryCode}"
}
