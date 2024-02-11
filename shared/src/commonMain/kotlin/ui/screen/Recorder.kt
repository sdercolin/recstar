package ui.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Square
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import repository.LocalAppPreferenceRepository
import repository.LocalKeyEventStore
import ui.common.plainClickable
import ui.model.LocalSafeAreaInsets
import ui.model.LocalScreenOrientation
import ui.model.ScreenOrientation
import ui.string.*
import ui.style.LocalThemeIsDarkMode
import util.alpha
import util.isMobile
import util.runIf

@Composable
fun Recorder(
    model: SessionScreenModel,
    hasFixedHeight: Boolean,
    isUpperLayer: Boolean,
) {
    val navigator = LocalNavigator.currentOrThrow
    val backgroundColor = if (isUpperLayer) {
        MaterialTheme.colors.surface
    } else {
        MaterialTheme.colors.background
    }
    LaunchRecordingKeyPress(model)
    Column(modifier = Modifier.fillMaxWidth().background(color = backgroundColor)) {
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
            if (LocalThemeIsDarkMode.current.not()) {
                Divider(color = MaterialTheme.colors.surface)
            }
            RecorderWaveform(
                hasFile = model.currentSentence.isFinished,
                isRecording = model.isRecording,
                flow = model.waveformFlow,
                playingProgress = model.playingProgress,
                isInteractionSuspended = model.isBusy,
                onTogglePlaying = model::togglePlaying,
                guideAudioName = model.guideAudioConfig?.name,
                onClickGuideAudioName = { navigator push GuideAudioScreen(model.name) },
            )
            if (LocalThemeIsDarkMode.current.not()) {
                Divider(color = MaterialTheme.colors.surface)
            }
        }
        val controlsAspectRatio = if (LocalScreenOrientation.current == ScreenOrientation.Landscape) {
            if (isMobile) 7f else 5f
        } else {
            3f
        }
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(controlsAspectRatio)) {
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
        modifier = Modifier.padding(horizontal = 32.dp).padding(bottom = 4.dp),
        style = MaterialTheme.typography.overline,
        fontSize = if (useSmallSizes) 8.sp else MaterialTheme.typography.overline.fontSize,
    )
    MainTitle(model, useSmallSizes)
    if (model.shouldShowSubTitle) {
        SubTitle(model, useSmallSizes)
    }
    Spacer(modifier = Modifier.height(if (useSmallSizes) 12.dp else 22.dp))
}

@Composable
private fun MainTitle(
    model: SessionScreenModel,
    useSmallSizes: Boolean,
) {
    val style = if (useSmallSizes) MaterialTheme.typography.h6 else MaterialTheme.typography.h4
    val text = model.getCurrentSentenceTitle()
    val boxHeight = if (useSmallSizes) 32.dp else 52.dp
    var fontSize by remember(text) { mutableStateOf(style.fontSize) }
    var maxLines by remember(text) { mutableStateOf(1) }
    var readyToDraw by remember(text) { mutableStateOf(false) }
    Box(Modifier.height(boxHeight).fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        Text(
            text = text,
            modifier = Modifier.padding(start = 32.dp, end = 16.dp)
                .drawWithContent {
                    if (readyToDraw) drawContent()
                },
            style = style,
            fontSize = fontSize,
            overflow = TextOverflow.Ellipsis,
            maxLines = maxLines,
            onTextLayout = {
                if (it.hasVisualOverflow) {
                    fontSize = fontSize.times(0.9f)
                    if (fontSize * maxLines < style.fontSize && maxLines < 3) {
                        maxLines++
                        fontSize = style.fontSize
                    }
                } else {
                    readyToDraw = true
                }
            },
        )
    }
}

@Composable
private fun SubTitle(
    model: SessionScreenModel,
    useSmallSizes: Boolean,
) {
    val topMargin = if (useSmallSizes) 4.dp else 8.dp
    val style = if (useSmallSizes) MaterialTheme.typography.body2 else MaterialTheme.typography.body1
    val text = model.getCurrentSentenceSubTitle().orEmpty()
    val boxHeight = if (useSmallSizes) 20.dp else 32.dp
    var fontSize by remember(text) { mutableStateOf(style.fontSize) }
    var maxLines by remember(text) { mutableStateOf(1) }
    var readyToDraw by remember(text) { mutableStateOf(false) }
    Box(
        modifier = Modifier.height(boxHeight).fillMaxWidth().padding(top = topMargin),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(start = 32.dp, end = 16.dp)
                .drawWithContent {
                    if (readyToDraw) drawContent()
                },
            style = style,
            fontSize = fontSize,
            overflow = TextOverflow.Ellipsis,
            maxLines = maxLines,
            onTextLayout = {
                if (it.hasVisualOverflow) {
                    fontSize = fontSize.times(0.9f)
                    if (fontSize * maxLines < style.fontSize && maxLines < 3) {
                        maxLines++
                        fontSize = style.fontSize
                    }
                } else {
                    readyToDraw = true
                }
            },
        )
    }
}

@Composable
private fun ColumnScope.RecorderWaveform(
    hasFile: Boolean,
    isRecording: Boolean,
    flow: Flow<Array<Array<FloatArray>>>,
    playingProgress: Float?,
    isInteractionSuspended: Boolean,
    onTogglePlaying: () -> Unit,
    guideAudioName: String?,
    onClickGuideAudioName: () -> Unit,
) {
    val isDarkMode = LocalThemeIsDarkMode.current
    val paperColor = if (isDarkMode) Color.Black else Color.White
    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(color = paperColor)) {
        val data by flow.collectAsState(initial = emptyArray())
        val color = if (isDarkMode) {
            if (isRecording) MaterialTheme.colors.secondary else MaterialTheme.colors.primary
        } else {
            if (isRecording) MaterialTheme.colors.primaryVariant else MaterialTheme.colors.onBackground
        }
        val playerCursorColor = if (isDarkMode) MaterialTheme.colors.secondary else MaterialTheme.colors.primaryVariant

        Column(modifier = Modifier.fillMaxSize().padding(bottom = 8.dp)) {
            for (channelIndex in data.indices) {
                Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    val width = size.width.toInt()
                    val height = size.height.toInt()
                    val halfHeight = height / 2
                    val channelData = data[channelIndex]
                    val dataLength = channelData.size
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
                        val dataInPoint = channelData.copyOfRange(dataPosStart, dataPosEnd)
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
            }
        }
        val iconTint = if (isDarkMode) {
            MaterialTheme.colors.primary.alpha(0.7f)
        } else {
            MaterialTheme.colors.onBackground.alpha(0.7f)
        }
        val iconSize = if (isMobile) 18.dp else 24.dp
        if (hasFile && !isRecording) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .clickable(enabled = isInteractionSuspended.not(), onClick = onTogglePlaying)
                    .padding(if (isMobile) 12.dp else 24.dp),
            ) {
                val icon = if (playingProgress != null) Icons.Default.Square else Icons.Default.PlayArrow
                val padding = if (playingProgress != null) 3.dp else 0.dp
                Icon(
                    modifier = Modifier.size(iconSize).padding(padding),
                    imageVector = icon,
                    contentDescription = string(Strings.SessionScreenTogglePlaying),
                    tint = iconTint,
                )
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .clickable(enabled = isRecording.not()) { onClickGuideAudioName() }
                .padding(if (isMobile) 12.dp else 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = guideAudioName ?: string(Strings.SessionScreenNoGuideAudio),
                style = MaterialTheme.typography.caption,
                fontSize = if (isMobile) 10.sp else MaterialTheme.typography.caption.fontSize,
                color = iconTint,
            )
            Spacer(modifier = Modifier.width(if (isMobile) 4.dp else 8.dp))
            Icon(
                modifier = Modifier.size(iconSize),
                imageVector = Icons.Default.MusicNote,
                contentDescription = string(Strings.SessionScreenActionConfigureGuideAudio),
                tint = iconTint,
            )
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
        modifier = Modifier.fillMaxSize()
            .padding(
                bottom = LocalSafeAreaInsets.current.bottomDp(
                    if (LocalScreenOrientation.current == ScreenOrientation.Landscape) 4f else 20f,
                ),
            ),
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
    val useSmallSizes = isMobile && LocalScreenOrientation.current == ScreenOrientation.Landscape
    val holdingMode = LocalAppPreferenceRepository.current.flow.collectAsState().value.recording.recordWhileHolding
    val heightRatio = if (useSmallSizes) 0.7f else 0.55f
    var holding by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    LaunchedEffect(isPressed) {
        if (isInteractionSuspended) return@LaunchedEffect
        if (isPressed) {
            if (!holding && !isRecording) {
                holding = true
                onToggleRecording()
            }
        } else {
            if (holding && isRecording) {
                holding = false
                onToggleRecording()
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxHeight(heightRatio)
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
            .runIf(holdingMode.not()) {
                plainClickable {
                    if (!isInteractionSuspended) {
                        onToggleRecording()
                    }
                }
            }
            .runIf(holdingMode) {
                clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) {
                    // handled with interactionSource
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
    val useSmallSizes = isMobile && LocalScreenOrientation.current == ScreenOrientation.Landscape
    val heightRatio = if (useSmallSizes) 0.55f else 0.4f
    IconButton(
        modifier = Modifier.fillMaxHeight(heightRatio)
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

@Composable
private fun LaunchRecordingKeyPress(model: SessionScreenModel) {
    val keyEventFlow = LocalKeyEventStore.current.flow
    var holding by remember { mutableStateOf(false) }
    val appPreference = LocalAppPreferenceRepository.current.flow.collectAsState()
    val recordingKey = appPreference.value.recording.recordingShortKey.getKey()
    val isHoldingMode = appPreference.value.recording.recordWhileHolding
    LaunchedEffect(keyEventFlow, isHoldingMode) {
        keyEventFlow.collectLatest { ev ->
            if (ev.key != recordingKey) return@collectLatest
            val isRecording = model.isRecording
            val isBusy = model.isBusy
            if (isHoldingMode) {
                when (ev.type) {
                    KeyEventType.KeyDown -> {
                        if (isRecording || isBusy || holding) return@collectLatest
                        holding = true
                        model.toggleRecording()
                    }
                    KeyEventType.KeyUp -> {
                        if (!isRecording || isBusy || !holding) return@collectLatest
                        holding = false
                        model.toggleRecording()
                    }
                    else -> return@collectLatest
                }
            } else {
                if (ev.type == KeyEventType.KeyUp) {
                    model.toggleRecording()
                }
            }
        }
    }
}
