package ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import ui.common.ReversedRow
import ui.common.ScrollableLazyColumn
import ui.common.plainClickable
import ui.model.LocalSafeAreaInsets
import ui.model.Sentence
import ui.style.CustomColors

private val itemHeight = 32.dp

@Composable
fun SentenceList(
    model: SessionScreenModel,
    isUpperLayer: Boolean,
) {
    val lazyListState = rememberLazyListState()
    val density = LocalDensity.current
    val itemHeightPx = remember(density) { with(density) { itemHeight.toPx() } }
    LaunchedEffect(Unit) {
        model.requestScrollToCurrentSentenceFlow.collectLatest {
            val targetOffset = with(lazyListState.layoutInfo) {
                (viewportEndOffset + viewportStartOffset) / 2
            }
            val additionalOffset = (targetOffset - itemHeightPx / 2).toInt()
            lazyListState.animateScrollToItem(
                index = model.currentIndex,
                scrollOffset = -additionalOffset,
            )
        }
    }
    val backgroundColor = if (isUpperLayer) {
        MaterialTheme.colors.surface
    } else {
        MaterialTheme.colors.background
    }
    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize().background(color = backgroundColor),
        lazyListState = lazyListState,
    ) {
        itemsIndexed(
            model.sentences,
            key = { index, _ -> index },
        ) { index, sentence ->
            SentenceItem(
                index = index,
                sentence = sentence,
                isCurrent = model.currentIndex == index,
                hideFinished = model.skipFinishedSentences,
                onClickItem = model::selectSentence,
            )
        }
    }
}

@Composable
private fun SentenceItem(
    index: Int,
    sentence: Sentence,
    isCurrent: Boolean,
    hideFinished: Boolean,
    onClickItem: (index: Int) -> Unit,
) {
    ReversedRow(
        modifier = Modifier.fillMaxWidth()
            .height(itemHeight)
            .background(color = if (isCurrent) MaterialTheme.colors.primary else Color.Transparent)
            .plainClickable { onClickItem(index) }
            .padding(
                start = 32.dp,
                end = 32.dp + LocalSafeAreaInsets.current.leftDp(20f),
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (sentence.isFinished) {
            val tint = when {
                isCurrent -> MaterialTheme.colors.onPrimary
                hideFinished -> CustomColors.DarkGreen.copy(alpha = 0.5f)
                else -> CustomColors.DarkGreen
            }
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = tint,
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        val textColor = when {
            isCurrent -> MaterialTheme.colors.onPrimary
            hideFinished && sentence.isFinished -> MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
            else -> MaterialTheme.colors.onBackground
        }
        Text(
            modifier = Modifier.weight(1f),
            text = sentence.text,
            style = MaterialTheme.typography.body2,
            color = textColor,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )
    }
}
