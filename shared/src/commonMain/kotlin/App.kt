import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import io.LocalFileInteractor
import io.Paths
import kotlinx.coroutines.flow.collectLatest
import model.Action
import model.Actions
import repository.LocalAppActionStore
import repository.LocalGuideAudioRepository
import repository.LocalReclistRepository
import ui.common.LocalAlertDialogController
import ui.common.LocalProgressController
import ui.common.LocalToastController
import ui.model.Screen
import ui.screen.AboutScreen
import ui.screen.MainScreen
import ui.string.*
import ui.style.AppTheme
import ui.style.LocalThemeIsDarkMode
import util.useIosStyle

@Composable
fun App() {
    val toastController = LocalToastController.current
    val alertDialogController = LocalAlertDialogController.current
    val progressController = LocalProgressController.current
    AppTheme(isDarkMode = LocalThemeIsDarkMode.current) {
        Navigator(MainScreen) { navigator ->
            MainScaffold(navigator)
        }
        alertDialogController.Compose()
        toastController.Compose()
        progressController.Compose()
    }
}

@Composable
private fun MainScaffold(navigator: Navigator) {
    val appActionStore = LocalAppActionStore.current
    val fileInteractor = LocalFileInteractor.current
    val reclistRepository = LocalReclistRepository.current
    val guideAudioRepository = LocalGuideAudioRepository.current
    val alertDialogController = LocalAlertDialogController.current
    val toastController = LocalToastController.current
    LaunchedEffect(navigator.lastItem) {
        appActionStore.onScreenChange(navigator.lastItem as Screen)
    }
    LaunchedEffect(appActionStore) {
        appActionStore.actions.collectLatest {
            when (it) {
                Action.ImportReclist -> Actions.importReclist(fileInteractor, reclistRepository, toastController)
                Action.ImportGuideAudio -> Actions.importGuideAudio(
                    fileInteractor,
                    guideAudioRepository,
                    alertDialogController,
                    toastController,
                )
                Action.OpenContentDirectory -> fileInteractor.requestOpenFolder(Paths.contentRoot)
                Action.OpenAppDirectory -> fileInteractor.requestOpenFolder(Paths.appRoot)
                Action.OpenAbout -> navigator push AboutScreen
                else -> Unit
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val currentTitle = (navigator.lastItem as Screen).getTitle()
                    Text(text = currentTitle)
                },
                navigationIcon = if (navigator.size > 1) {
                    @Composable {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = if (useIosStyle) {
                                    Icons.Default.ArrowBackIosNew
                                } else {
                                    Icons.Default.ArrowBack
                                },
                                contentDescription = string(Strings.CommonBack),
                            )
                        }
                    }
                } else {
                    null
                },
                actions = {
                    (navigator.lastItem as Screen).Actions()
                },
            )
        },
        content = {
            SlideTransition(navigator) { screen ->
                Surface { screen.Content() }
            }
        },
    )
}
