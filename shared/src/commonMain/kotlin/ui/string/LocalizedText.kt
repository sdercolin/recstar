package ui.string

import androidx.compose.runtime.Composable

interface LocalizedText {
    val textKey: Strings

    @Composable
    fun getText(): String = string(textKey)
}
