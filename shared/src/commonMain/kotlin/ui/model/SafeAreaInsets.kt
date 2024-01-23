package ui.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class SafeAreaInsets(
    val top: Float,
    val left: Float,
    val bottom: Float,
    val right: Float,
)

val LocalSafeAreaInsets = staticCompositionLocalOf {
    SafeAreaInsets(0f, 0f, 0f, 0f)
}
