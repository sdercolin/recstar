package ui.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Square
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.Flow
import ui.common.plainClickable
import ui.model.LocalScreenOrientation
import ui.model.ScreenOrientation
import ui.string.*
import util.alpha
import util.isMobile

@Composable
fun Recorder(
    model: SessionScreenModel,
    hasFixedHeight: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth().background(color = MaterialTheme.colors.background)) {
        RecorderTitleBar(model)
        Column(
            modifier = Modifier.fillMaxWidth()
                .run {
                    if (hasFixedHeight) {
                        weight(1f)
                    } else {
                        aspectRatio(3f)
                    }
                },
        ) {
            if (isSystemInDarkTheme().not()) {
                Divider(color = MaterialTheme.colors.surface)
            }
            RecorderWaveform(
                hasFile = model.currentSentence.isFinished,
                isRecording = model.isRecording,
                flow = model.waveformFlow,
                playingProgress = model.playingProgress,
                isInteractionSuspended = model.isBusy,
                onTogglePlaying = model::togglePlaying,
            )
            if (isSystemInDarkTheme().not()) {
                Divider(color = MaterialTheme.colors.surface)
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth()
                .aspectRatio(
                    if (LocalScreenOrientation.current == ScreenOrientation.Landscape) 5f else 3f,
                ),
        ) {
            RecorderControls(
                isInteractionSuspended = model.isBusy,
                isPlaying = model.isPlaying,
                isRecording = model.isRecording,
                onToggleRecording = model::toggleRecording,
                hasNext = model.hasNext,
                onNext = model::next,
                hasPrevious = model.hasPrevious,
                onPrevious = model::previous,
            )
        }
    }
}

@Composable
private fun RecorderTitleBar(model: SessionScreenModel) {
    val useSmallSizes = isMobile && LocalScreenOrientation.current == ScreenOrientation.Landscape
    Spacer(modifier = Modifier.height(if (useSmallSizes) 12.dp else 24.dp))
    Text(
        text = string(Strings.SessionScreenCurrentSentenceLabel),
        modifier = Modifier.padding(horizontal = 32.dp),
        style = MaterialTheme.typography.overline,
        fontSize = if (useSmallSizes) 8.sp else MaterialTheme.typography.overline.fontSize,
    )
    val style = if (useSmallSizes) MaterialTheme.typography.h6 else MaterialTheme.typography.h4
    val text = model.currentSentence.text
    val boxHeight = if (useSmallSizes) 32.dp else 52.dp
    var fontSize by remember(text) { mutableStateOf(style.fontSize) }
    var readyToDraw by remember(text) { mutableStateOf(false) }
    Box(Modifier.height(boxHeight).fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        Text(
            text = model.currentSentence.text,
            modifier = Modifier.padding(start = 32.dp, end = 16.dp)
                .drawWithContent {
                    if (readyToDraw) drawContent()
                },
            style = style,
            fontSize = fontSize,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            onTextLayout = {
                if (it.hasVisualOverflow) {
                    fontSize = fontSize.times(0.9f)
                } else {
                    readyToDraw = true
                }
            },
        )
    }
    Spacer(modifier = Modifier.height(if (useSmallSizes) 12.dp else 24.dp))
}

@Composable
private fun ColumnScope.RecorderWaveform(
    hasFile: Boolean,
    isRecording: Boolean,
    flow: Flow<Array<FloatArray>>,
    playingProgress: Float?,
    isInteractionSuspended: Boolean,
    onTogglePlaying: () -> Unit,
) {
    val isDarkMode = isSystemInDarkTheme()
    val paperColor = if (isDarkMode) Color.Black else Color.White
    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(color = paperColor)) {
        if (!hasFile && !isRecording) {
            Text(
                text = string(Strings.SessionScreenNoData),
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onBackground,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        val data by flow.collectAsState(initial = emptyArray())
        val color = if (isDarkMode) MaterialTheme.colors.primary else MaterialTheme.colors.onBackground
        val playerCursorColor = if (isDarkMode) MaterialTheme.colors.secondary else MaterialTheme.colors.primaryVariant
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width.toInt()
            val height = size.height.toInt()
            val halfHeight = height / 2
            val dataLength = data.size
            val ratio = (dataLength.toFloat() / width).coerceAtLeast(1f)
            val offset = (dataLength - width).coerceAtMost(0)
            for (i in 0 until width) {
                val x = i.toFloat()
                val dataPosStart = (i * ratio).toInt() + offset
                val dataPosEnd = ((i + 1) * ratio).toInt() + offset
                if (dataPosStart < 0 || dataPosStart >= dataLength ||
                    dataPosEnd - 1 < 0 || dataPosEnd - 1 >= dataLength
                ) {
                    continue
                }
                val dataInPoint = data.copyOfRange(dataPosStart, dataPosEnd)
                val max = dataInPoint.maxOfOrNull { it[0] } ?: continue
                val min = dataInPoint.minOfOrNull { it[1] } ?: continue
                val maxY = max * halfHeight
                val minY = min * halfHeight
                drawLine(
                    color = color,
                    start = Offset(x, halfHeight + maxY),
                    end = Offset(x, halfHeight + minY),
                )
            }
            if (playingProgress != null && playingProgress in 0f..1f) {
                val x = playingProgress * width
                drawLine(
                    color = playerCursorColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height.toFloat()),
                    strokeWidth = 2f,
                )
            }
        }
        if (hasFile && !isRecording) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .clickable(enabled = isInteractionSuspended.not(), onClick = onTogglePlaying)
                    .padding(if (isMobile) 12.dp else 24.dp),
            ) {
                val icon = if (playingProgress != null) Icons.Default.Square else Icons.Default.PlayArrow
                val size = if (isMobile) 20.dp else 24.dp
                val padding = if (playingProgress != null) 3.dp else 0.dp
                val tint = if (isDarkMode) {
                    MaterialTheme.colors.primary.alpha(0.7f)
                } else {
                    MaterialTheme.colors.primaryVariant
                }
                Icon(
                    modifier = Modifier.size(size).padding(padding),
                    imageVector = icon,
                    contentDescription = string(Strings.SessionScreenTogglePlaying),
                    tint = tint,
                )
            }
        }
    }
}

@Composable
private fun RecorderControls(
    isInteractionSuspended: Boolean,
    isPlaying: Boolean,
    isRecording: Boolean,
    onToggleRecording: () -> Unit,
    hasNext: Boolean,
    onNext: () -> Unit,
    hasPrevious: Boolean,
    onPrevious: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavigateButton(
            isInteractionSuspended = isInteractionSuspended,
            onClick = onPrevious,
            enabled = hasPrevious,
            imageVector = Icons.Default.NavigateBefore,
            contentDescription = "",
        )
        Spacer(modifier = Modifier.width(48.dp))
        RecordButton(
            isInteractionSuspended = isInteractionSuspended || isPlaying,
            isRecording = isRecording,
            onToggleRecording = onToggleRecording,
        )
        Spacer(modifier = Modifier.width(48.dp))
        NavigateButton(
            isInteractionSuspended = isInteractionSuspended,
            onClick = onNext,
            enabled = hasNext,
            imageVector = Icons.Default.NavigateNext,
            contentDescription = "",
        )
    }
}

@Composable
private fun RecordButton(
    isInteractionSuspended: Boolean,
    isRecording: Boolean,
    onToggleRecording: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxHeight(0.55f)
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
            .plainClickable {
                if (!isInteractionSuspended) {
                    onToggleRecording()
                }
            },
    ) {
        Crossfade(targetState = isRecording) { isRecording ->
            val imageVector = if (isRecording) Icons.Filled.Square else Icons.Filled.Circle
            val sizeFraction = if (isRecording) 0.7f else 1f
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.fillMaxSize(sizeFraction),
                    imageVector = imageVector,
                    contentDescription = "",
                    tint = MaterialTheme.colors.error,
                )
            }
        }
    }
}

@Composable
private fun NavigateButton(
    isInteractionSuspended: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    imageVector: ImageVector,
    contentDescription: String,
) {
    IconButton(
        modifier = Modifier.fillMaxHeight(0.4f)
            .aspectRatio(1f, matchHeightConstraintsFirst = true),
        onClick = {
            if (!isInteractionSuspended) {
                onClick()
            }
        },
        enabled = enabled,
    ) {
        CompositionLocalProvider(
            LocalContentAlpha provides if (enabled) ContentAlpha.high else ContentAlpha.disabled,
        ) {
            Icon(
                modifier = Modifier.fillMaxSize(),
                imageVector = imageVector,
                contentDescription = contentDescription,
            )
        }
    }
}
