package ui.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A data class that represents the safe area insets in pixels.
 */
@Immutable
data class SafeAreaInsets(
    val top: Float,
    val left: Float,
    val bottom: Float,
    val right: Float,
) {
    @Composable
    private fun getDp(
        px: Float,
        reduce: Float,
        min: Float,
    ): Dp = with(LocalDensity.current) { (px.toDp().value - reduce).coerceAtLeast(min).dp }

    @Composable
    fun topDp(
        reduce: Float = 0f,
        min: Float = 0f,
    ): Dp = getDp(top, reduce, min)

    @Composable
    fun leftDp(
        reduce: Float = 0f,
        min: Float = 0f,
    ): Dp = getDp(left, reduce, min)

    @Composable
    fun bottomDp(
        reduce: Float = 0f,
        min: Float = 0f,
    ): Dp = getDp(bottom, reduce, min)

    @Composable
    fun rightDp(
        reduce: Float = 0f,
        min: Float = 0f,
    ): Dp = getDp(right, reduce, min)
}

val LocalSafeAreaInsets = staticCompositionLocalOf {
    SafeAreaInsets(0f, 0f, 0f, 0f)
}
