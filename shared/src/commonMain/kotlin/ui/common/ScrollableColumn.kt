package ui.common

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import util.isDesktop
import util.runIf

@Composable
fun ScrollableColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    wrapWidth: Boolean = false,
    scrollState: ScrollState = rememberScrollState(),
    forceScrollBar: Boolean? = null,
    scrollBarModifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val showScrollBar = forceScrollBar ?: isDesktop
    if (showScrollBar) {
        Box(modifier = modifier) {
            Column(
                modifier = Modifier.wrapContentWidth()
                    .runIf(!wrapWidth) { fillMaxWidth() }
                    .verticalScroll(scrollState),
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
                content = content,
            )
            VerticalScrollbar(
                modifier = scrollBarModifier.align(Alignment.CenterEnd),
                scrollState = scrollState,
            )
        }
    } else {
        Column(
            modifier = modifier.verticalScroll(scrollState),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            content = content,
        )
    }
}
