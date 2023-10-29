package ui.common

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VerticalScrollbar(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
)

@Composable
expect fun VerticalScrollbar(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
)
