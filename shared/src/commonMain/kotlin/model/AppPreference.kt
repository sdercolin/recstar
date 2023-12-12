package model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import kotlinx.serialization.Serializable
import ui.string.*
import util.isDesktop

private typealias StringLanguage = Language

@Immutable
@Serializable
data class AppPreference(
    val language: Language = Language.Auto,
    val theme: Theme = if (isDesktop) Theme.Dark else Theme.System,
    val customContentRootPath: String? = null,
) : JavaSerializable {
    enum class Language(private val language: StringLanguage?) : LocalizedTest {
        Auto(null),
        English(StringLanguage.English),
        SimplifiedChinese(StringLanguage.ChineseSimplified),
        Japanese(StringLanguage.Japanese),
        ;

        fun getLanguage(): StringLanguage = language ?: findBestMatchedLanguage()

        @Composable
        override fun getText(): String = language?.displayName ?: string(Strings.PreferenceLanguageAuto)
    }

    enum class Theme(private val textKey: Strings) : LocalizedTest {
        System(Strings.PreferenceThemeSystem),
        Light(Strings.PreferenceThemeLight),
        Dark(Strings.PreferenceThemeDark),
        ;

        @Composable
        override fun getText(): String = string(textKey)
    }
}
