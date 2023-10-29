package ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

@Composable
inline fun ReversedRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    crossinline content: @Composable RowScope.() -> Unit,
) {
    val direction = LocalLayoutDirection.current
    val reversedDirection = when (direction) {
        LayoutDirection.Ltr -> LayoutDirection.Rtl
        LayoutDirection.Rtl -> LayoutDirection.Ltr
    }
    CompositionLocalProvider(LocalLayoutDirection provides reversedDirection) {
        Row(modifier, horizontalArrangement, verticalAlignment) {
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                content.invoke(this@Row)
            }
        }
    }
}
