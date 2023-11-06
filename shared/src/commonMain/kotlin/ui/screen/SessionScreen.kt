package ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import io.ExportDataRequest
import io.LocalFileInteractor
import model.Session
import ui.common.LocalAlertDialogController
import ui.common.LocalProgressController
import ui.common.requestInput
import ui.model.LocalScreenOrientation
import ui.model.Screen
import ui.model.ScreenOrientation
import ui.string.*
import util.isDesktop
import util.isIos
import util.isMobile

data class SessionScreen(val initialSession: Session) : Screen {
    @Composable
    override fun getTitle(): String = rememberSessionScreenModel(initialSession).name

    @Composable
    override fun Actions() = ScreenActions()

    @Composable
    override fun Content() = ScreenContent()
}

@Composable
private fun SessionScreen.ScreenActions() {
    var showMenu by remember { mutableStateOf(false) }
    val model = rememberSessionScreenModel(initialSession)
    IconButton(
        enabled = model.isRecording.not() && model.isBusy.not(),
        onClick = { showMenu = !showMenu },
    ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = string(Strings.CommonMore),
        )
    }
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false },
    ) {
        val fileInteractor = LocalFileInteractor.current
        val progressController = LocalProgressController.current
        val alertDialogController = LocalAlertDialogController.current
        val useOpenDirectory = isDesktop || isIos
        val useExport = isMobile
        if (useOpenDirectory) {
            DropdownMenuItem(
                onClick = {
                    showMenu = false
                    fileInteractor.requestOpenFolder(model.contentDirectory)
                },
            ) {
                Text(text = string(Strings.SessionScreenActionOpenDirectory))
            }
        }
        if (useExport) {
            DropdownMenuItem(
                onClick = {
                    showMenu = false
                    val request = ExportDataRequest(
                        folder = model.contentDirectory,
                        allowedExtension = listOf("wav"),
                        onStart = { progressController.show() },
                        onSuccess = { progressController.hide() },
                        onCancel = { progressController.hide() },
                        onError = { progressController.hide() },
                    )
                    fileInteractor.exportData(request)
                },
            ) {
                Text(text = string(Strings.SessionScreenActionExport))
            }
        }
        DropdownMenuItem(
            onClick = {
                showMenu = false
                alertDialogController.requestInput(
                    title = stringStatic(Strings.SessionScreenActionRenameSession),
                    initialValue = model.name,
                    selected = true,
                    onConfirmInput = model::renameSession,
                )
            },
        ) {
            Text(text = string(Strings.SessionScreenActionRenameSession))
        }
    }
}

@Composable
private fun SessionScreen.ScreenContent() {
    val model = rememberSessionScreenModel(initialSession)
    val screenOrientation = LocalScreenOrientation.current
    if (screenOrientation == ScreenOrientation.Landscape) {
        Layout(
            modifier = Modifier.fillMaxSize(),
            content = {
                Surface(
                    modifier = Modifier.fillMaxHeight(),
                    elevation = 2.dp,
                ) {
                    SentenceList(model)
                }
                Box(modifier = Modifier.fillMaxHeight()) {
                    Recorder(model, hasFixedHeight = true)
                }
            },
        ) { measureables, constraints ->
            val maxWidth = constraints.maxWidth
            val sentenceListWidth = (maxWidth * 0.3f).toInt()
            val recorderWidth = (maxWidth * 0.7f).toInt()
            val sentenceList = measureables[0].measure(
                constraints.copy(
                    minWidth = sentenceListWidth,
                    maxWidth = sentenceListWidth,
                ),
            )
            val recorder = measureables[1].measure(
                constraints.copy(
                    minWidth = recorderWidth,
                    maxWidth = recorderWidth,
                ),
            )
            layout(constraints.maxWidth, constraints.maxHeight) {
                recorder.placeRelative(sentenceListWidth, 0)
                sentenceList.placeRelative(0, 0)
            }
        }
    } else {
        Layout(
            modifier = Modifier.fillMaxSize(),
            content = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 2.dp,
                ) {
                    SentenceList(model)
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    Recorder(model, hasFixedHeight = false)
                }
            },
        ) { measureables, constraints ->
            val recorder = measureables[1].measure(constraints.copy(minHeight = 0))
            val remainingHeight = constraints.maxHeight - recorder.height
            val sentenceList = measureables[0].measure(
                constraints.copy(
                    minHeight = remainingHeight,
                    maxHeight = remainingHeight,
                ),
            )
            layout(constraints.maxWidth, constraints.maxHeight) {
                recorder.placeRelative(0, remainingHeight)
                sentenceList.placeRelative(0, 0)
            }
        }
    }
}
