import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import const.APP_NAME
import io.Paths
import io.ensurePath
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import model.AppRecord
import repository.AppRecordRepository
import ui.model.DesktopContext
import util.Log
import util.toJavaFile

fun main() =
    application {
        remember {
            ensurePath(Paths.appRoot)
            Log.initialize()
        }

        val coroutineScope = rememberCoroutineScope()
        val context = remember { DesktopContext(coroutineScope) }
        val dependencies = remember(context) { AppDependencies(context) }
        ensureContentRoot(dependencies)
        val windowState = rememberResizableWindowState(dependencies.appRecordRepository.stateFlow)

        ProvideAppDependencies(dependencies) {
            Window(
                title = APP_NAME,
                icon = painterResource("icon.ico"),
                state = windowState,
                onCloseRequest = ::exitApplication,
            ) {
                LaunchSaveWindowSize(windowState, dependencies.appRecordRepository)
                MainView()
                Menu()
            }
        }
    }

@Composable
private fun LaunchSaveWindowSize(
    windowState: WindowState,
    appRecordRepository: AppRecordRepository,
) {
    LaunchedEffect(windowState) {
        snapshotFlow { windowState.size }
            .onEach(appRecordRepository::saveWindowSize)
            .launchIn(this)
    }
}

@Composable
private fun rememberResizableWindowState(appRecord: StateFlow<AppRecord>): WindowState {
    val windowSize = remember { appRecord.value.windowSizeDp }
    return rememberWindowState(width = windowSize.first.dp, height = windowSize.second.dp)
}

private fun AppRecordRepository.saveWindowSize(dpSize: DpSize) {
    val size = dpSize.width.value to dpSize.height.value
    update { copy(windowSizeDp = size) }
}

@Composable
private fun ensureContentRoot(dependencies: AppDependencies) =
    remember {
        val file = dependencies.appPreferenceRepository.value.customContentRootPath?.toJavaFile()
        Paths.customContentRootLocation = file
        ensurePath(Paths.contentRoot)
        if (file != null) {
            dependencies.reclistRepository.init()
            dependencies.guideAudioRepository.init()
            dependencies.sessionRepository.init()
        }
    }
