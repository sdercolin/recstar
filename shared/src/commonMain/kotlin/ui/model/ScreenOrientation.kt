package ui.model

import androidx.compose.runtime.staticCompositionLocalOf
import ui.model.ScreenOrientation.Landscape

/**
 * This enum is used to provide the current screen orientation to the shared code.
 * - On Android/iOS, actual screen orientation is used.
 * - On Desktop, currently only [Landscape] is supported.
 */
enum class ScreenOrientation {
    Portrait,
    Landscape,
    Undefined,
}

val LocalScreenOrientation = staticCompositionLocalOf<ScreenOrientation> {
    error("No ScreenOrientation provided!")
}
