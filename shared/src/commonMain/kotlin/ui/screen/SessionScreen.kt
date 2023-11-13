package ui.screen

import LocalAppActionStore
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Surface
import androidx.compose.material.Text
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
import repository.LocalReclistRepository
import ui.common.ActionMenu
import ui.common.LocalAlertDialogController
import ui.common.LocalProgressController
import ui.common.LocalToastController
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
    ActionMenu { closeMenu ->
        val fileInteractor = LocalFileInteractor.current
        val progressController = LocalProgressController.current
        val alertDialogController = LocalAlertDialogController.current
        val useOpenDirectory = isDesktop || isIos
        val useExport = isMobile
        if (useOpenDirectory) {
            DropdownMenuItem(
                onClick = {
                    closeMenu()
                    Actions.openDirectory(fileInteractor, model.contentDirectory)
                },
            ) {
                Text(text = string(Strings.SessionScreenActionOpenDirectory))
            }
        }
        if (useExport) {
            DropdownMenuItem(
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
            ) {
                Text(text = string(Strings.SessionScreenActionExport))
            }
        }
        DropdownMenuItem(
            onClick = {
                closeMenu()
                Actions.renameSession(alertDialogController, model)
            },
        ) {
            Text(text = string(Strings.SessionScreenActionRenameSession))
        }
    }
}

@Composable
private fun SessionScreen.ScreenContent() {
    val model = rememberSessionScreenModel(initialSession)
    val navigator = LocalNavigator.currentOrThrow
    val screenOrientation = LocalScreenOrientation.current
    val actionStore = LocalAppActionStore.current
    val fileInteractor = LocalFileInteractor.current
    val reclistRepository = LocalReclistRepository.current
    val toastController = LocalToastController.current
    val alertDialogController = LocalAlertDialogController.current
    LaunchedEffect(model, actionStore) {
        actionStore.actions.collectLatest { action ->
            when (action) {
                Action.NewSession -> {
                    navigator.popUntilRoot()
                    navigator push CreateSessionReclistScreen
                }
                Action.ImportReclist -> Actions.importReclist(fileInteractor, reclistRepository, toastController)
                Action.OpenDirectory -> Actions.openDirectory(fileInteractor, model.contentDirectory)
                Action.Exit -> navigator.pop()
                Action.RenameSession -> Actions.renameSession(alertDialogController, model)
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
