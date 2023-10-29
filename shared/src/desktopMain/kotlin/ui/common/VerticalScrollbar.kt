package ui.common

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
actual fun VerticalScrollbar(
    modifier: Modifier,
    scrollState: ScrollState,
) {
    androidx.compose.foundation.VerticalScrollbar(
        modifier = modifier.width(15.dp),
        adapter = rememberScrollbarAdapter(scrollState),
    )
}

@Composable
actual fun VerticalScrollbar(
    modifier: Modifier,
    lazyListState: LazyListState,
) {
    androidx.compose.foundation.VerticalScrollbar(
        modifier = modifier.width(15.dp),
        adapter = rememberScrollbarAdapter(lazyListState),
    )
}
