package ui.string

import androidx.compose.runtime.compositionLocalOf
import util.Locale
import util.Log

var currentLanguage: Language = Language.default

val LocalLanguage = compositionLocalOf { Language.default }

enum class Language(val code: String, val displayName: String) {
    English("en", "English"),
    ChineseSimplified("zh-Hans", "简体中文"),
    Japanese("ja", "日本語"),
    ;

    companion object {
        val default = English

        fun find(languageTag: String): Language? {
            for (value in values()) {
                val codeLevels = value.code.split("-").scan("") { acc, s ->
                    if (acc.isEmpty()) s else "$acc-$s"
                }.filter { it.isNotEmpty() }
                for (code in codeLevels.reversed()) {
                    if (languageTag.startsWith(code)) {
                        return value
                    }
                }
            }
            return null
        }
    }
}

fun findBestMatchedLanguage(): Language {
    val detected = Language.find(Locale)
    Log.i("Locale: $Locale, Language: $detected")
    return detected ?: Language.default
}
