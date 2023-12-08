package util

import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.preferredLanguages

actual val Locale: String by lazy {
    Log.d("preferredLanguages: ${NSLocale.preferredLanguages.joinToString(", ")}")
    val preferredLanguage = NSLocale.preferredLanguages.firstOrNull() as? String
    if (preferredLanguage != null) {
        return@lazy preferredLanguage
    }
    val locale = NSLocale.currentLocale
    "${locale.languageCode}-${locale.countryCode}"
}
