package ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ExportDataRequest
import io.LocalFileInteractor
import kotlinx.coroutines.flow.collectLatest
import model.Action
import model.Actions
import model.Session
import repository.LocalAppActionStore
import ui.common.ActionMenu
import ui.common.ActionMenuItem
import ui.common.LocalAlertDialogController
import ui.common.LocalProgressController
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
    val model = rememberSessionScreenModel(initialSession)
    val navigator = LocalNavigator.currentOrThrow
    ActionMenu { closeMenu ->
        val fileInteractor = LocalFileInteractor.current
        val progressController = LocalProgressController.current
        val alertDialogController = LocalAlertDialogController.current
        val useOpenDirectory = isDesktop || isIos
        val useExport = isMobile
        if (useOpenDirectory) {
            ActionMenuItem(
                text = string(Strings.SessionScreenActionOpenDirectory),
                icon = Icons.Default.Folder,
                onClick = {
                    closeMenu()
                    Actions.openDirectory(fileInteractor, model.contentDirectory)
                },
            )
        }
        if (useExport) {
            ActionMenuItem(
                text = string(Strings.SessionScreenActionExport),
                icon = Icons.Default.IosShare,
                onClick = {
                    closeMenu()
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
            )
        }
        ActionMenuItem(
            text = string(Strings.SessionScreenActionRenameSession),
            icon = Icons.Default.Edit,
            onClick = {
                closeMenu()
                Actions.renameSession(alertDialogController, model)
            },
        )
        ActionMenuItem(
            text = string(Strings.SessionScreenActionConfigureGuideAudio),
            icon = Icons.Default.MusicNote,
            onClick = {
                closeMenu()
                navigator push GuideAudioScreen(model.name)
            },
        )
    }
}

@Composable
private fun SessionScreen.ScreenContent() {
    val model = rememberSessionScreenModel(initialSession)
    val navigator = LocalNavigator.currentOrThrow
    val screenOrientation = LocalScreenOrientation.current
    val actionStore = LocalAppActionStore.current
    val fileInteractor = LocalFileInteractor.current
    val alertDialogController = LocalAlertDialogController.current
    LaunchedEffect(model, actionStore) {
        actionStore.actions.collectLatest { action ->
            when (action) {
                Action.NewSession -> {
                    navigator.popUntilRoot()
                    navigator push CreateSessionReclistScreen
                }
                Action.OpenDirectory -> Actions.openDirectory(fileInteractor, model.contentDirectory)
                Action.Exit -> navigator.pop()
                Action.RenameSession -> Actions.renameSession(alertDialogController, model)
                Action.ConfigureGuideAudio -> navigator push GuideAudioScreen(model.name)
                Action.ToggleRecording -> model.toggleRecording()
                Action.NextSentence -> model.next()
                Action.PreviousSentence -> model.previous()
                else -> Unit
            }
        }
    }
    if (screenOrientation == ScreenOrientation.Landscape) {
        Layout(
            modifier = Modifier.fillMaxSize(),
            content = {
                Surface(
                    modifier = Modifier.fillMaxHeight(),
                    elevation = 10.dp,
                ) {
                    SentenceList(model, isUpperLayer = true)
                }
                Box(modifier = Modifier.fillMaxHeight()) {
                    Recorder(model, hasFixedHeight = true, isUpperLayer = false)
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
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                SentenceList(model, isUpperLayer = false)
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                elevation = 10.dp,
            ) {
                Recorder(model, hasFixedHeight = false, isUpperLayer = true)
            }
        }
    }
}
