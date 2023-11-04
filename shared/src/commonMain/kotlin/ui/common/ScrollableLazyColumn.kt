package ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import util.isDesktop
import util.runIf

@Composable
fun ScrollableLazyColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    wrapWidth: Boolean = false,
    lazyListState: LazyListState = rememberLazyListState(),
    forceScrollBar: Boolean? = null,
    scrollBarModifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    val showScrollBar = forceScrollBar ?: isDesktop
    if (showScrollBar) {
        Box(modifier = modifier) {
            LazyColumn(
                modifier = Modifier.wrapContentWidth()
                    .runIf(!wrapWidth) { fillMaxWidth() },
                state = lazyListState,
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
                content = content,
            )
            VerticalScrollbar(
                modifier = scrollBarModifier.align(Alignment.CenterEnd),
                lazyListState = lazyListState,
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            state = lazyListState,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            content = content,
        )
    }
}
