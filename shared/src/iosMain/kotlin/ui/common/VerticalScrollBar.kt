package ui.common

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun VerticalScrollbar(
    modifier: Modifier,
    scrollState: ScrollState,
) {
    // no scrollbar on mobile platforms
}
