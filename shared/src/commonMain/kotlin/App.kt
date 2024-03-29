import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import io.LocalFileInteractor
import io.Paths
import kotlinx.coroutines.flow.collectLatest
import model.Action
import model.Actions
import repository.LocalAppActionStore
import repository.LocalAppPreferenceRepository
import repository.LocalAppRecordRepository
import repository.LocalGuideAudioRepository
import repository.LocalReclistRepository
import ui.common.LocalAlertDialogController
import ui.common.LocalProgressController
import ui.common.LocalToastController
import ui.encoding.LocalTextEncodingDialogController
import ui.model.LocalAppContext
import ui.model.LocalSafeAreaInsets
import ui.model.Screen
import ui.screen.AboutScreen
import ui.screen.MainScreen
import ui.screen.PreferenceScreen
import ui.string.*
import ui.style.AppTheme
import ui.style.LocalThemeIsDarkMode
import util.isIos
import util.useIosStyle

@Composable
fun App() {
    val toastController = LocalToastController.current
    val alertDialogController = LocalAlertDialogController.current
    val progressController = LocalProgressController.current
    val recordRepository = LocalAppRecordRepository.current
    val preferenceRepository = LocalAppPreferenceRepository.current
    val textEncodingDialogController = LocalTextEncodingDialogController.current
    remember { Migrations.run(recordRepository, preferenceRepository) }
    AppTheme(isDarkMode = LocalThemeIsDarkMode.current) {
        Navigator(MainScreen) { navigator ->
            MainScaffold(navigator)
        }
        textEncodingDialogController.Compose()
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
    val appPreferenceRepository = LocalAppPreferenceRepository.current
    val alertDialogController = LocalAlertDialogController.current
    val toastController = LocalToastController.current
    val textEncodingDialogController = LocalTextEncodingDialogController.current
    val context = LocalAppContext.current
    LaunchedEffect(navigator.lastItem) {
        appActionStore.onScreenChange(navigator.lastItem as Screen)
    }
    LaunchedEffect(appActionStore) {
        appActionStore.actions.collectLatest {
            when (it) {
                Action.ImportReclist -> Actions.importReclist(
                    context.coroutineScope,
                    fileInteractor,
                    reclistRepository,
                    alertDialogController,
                    toastController,
                    textEncodingDialogController,
                    appPreferenceRepository,
                )
                Action.ImportGuideAudio -> Actions.importGuideAudio(
                    context.coroutineScope,
                    fileInteractor,
                    guideAudioRepository,
                    alertDialogController,
                    toastController,
                )
                Action.OpenContentDirectory -> fileInteractor.requestOpenFolder(Paths.contentRoot)
                Action.OpenAppDirectory -> fileInteractor.requestOpenFolder(Paths.appRoot)
                Action.OpenAbout -> navigator push AboutScreen
                Action.OpenSettings -> navigator push PreferenceScreen
                Action.ClearSettings -> Actions.clearSettings(alertDialogController, appPreferenceRepository)
                Action.ClearAppData -> Actions.clearAppData(alertDialogController)
                else -> Unit
            }
        }
    }
    Scaffold(
        topBar = {
            TopBar(
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
                contentPadding = PaddingValues(
                    top = LocalSafeAreaInsets.current.topDp(reduce = if (isIos) 8f else 0f),
                    start = LocalSafeAreaInsets.current.leftDp(reduce = 28f, min = 4f),
                    end = LocalSafeAreaInsets.current.rightDp(reduce = 28f, min = 4f),
                ),
            )
        },
        content = {
            SlideTransition(navigator) { screen ->
                Surface { screen.Content() }
            }
        },
    )
}

@Composable
private fun TopBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    contentPadding: PaddingValues,
) {
    TopAppBar(contentPadding = contentPadding) {
        if (navigationIcon == null) {
            Spacer(Modifier.width(12.dp))
        } else {
            Row(Modifier.fillMaxHeight().width(68.dp), verticalAlignment = Alignment.CenterVertically) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.high,
                    content = navigationIcon,
                )
            }
        }

        Row(
            Modifier.fillMaxHeight().weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProvideTextStyle(value = MaterialTheme.typography.h6) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.high,
                    content = title,
                )
            }
        }

        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Row(
                Modifier.fillMaxHeight(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }
    }
}
