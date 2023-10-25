package ui.model

import androidx.compose.runtime.staticCompositionLocalOf

enum class ScreenOrientation {
    Portrait,
    Landscape,
    Undefined,
}

val LocalScreenOrientation = staticCompositionLocalOf<ScreenOrientation> {
    error("No ScreenOrientation provided!")
}
