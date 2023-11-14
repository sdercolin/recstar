import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import io.FileInteractor
import io.LocalFileInteractor
import io.LocalPermissionChecker
import io.PermissionChecker
import model.AppPreference
import repository.AppActionStore
import repository.AppPreferenceRepository
import repository.AppRecordRepository
import repository.LocalAppActionStore
import repository.LocalAppPreferenceRepository
import repository.LocalAppRecordRepository
import repository.LocalReclistRepository
import repository.LocalSessionRepository
import repository.ReclistRepository
import repository.SessionRepository
import repository.createAppPreferenceRepository
import repository.createAppRecordRepository
import ui.common.AlertDialogController
import ui.common.LocalAlertDialogController
import ui.common.LocalProgressController
import ui.common.LocalToastController
import ui.common.ProgressController
import ui.common.ToastController
import ui.model.AppContext
import ui.model.LocalAppContext
import ui.string.*
import ui.style.LocalThemeIsDarkMode

/**
 * A class to hold all static dependencies of the app, bound to the lifecycle of the [context].
 */
class AppDependencies(
    val context: AppContext,
    val appRecordRepository: AppRecordRepository = createAppRecordRepository(context.coroutineScope),
    val appPreferenceRepository: AppPreferenceRepository = createAppPreferenceRepository(),
    val appActionStore: AppActionStore = AppActionStore(context.coroutineScope),
    val toastController: ToastController = ToastController(context),
    val alertDialogController: AlertDialogController = AlertDialogController(context),
    val progressController: ProgressController = ProgressController(),
    val fileInteractor: FileInteractor = FileInteractor(context, toastController, alertDialogController),
    val permissionChecker: PermissionChecker = PermissionChecker(context),
    val reclistRepository: ReclistRepository = ReclistRepository(),
    val sessionRepository: SessionRepository = SessionRepository(reclistRepository),
)

@Composable
fun ProvideAppDependencies(
    dependencies: AppDependencies,
    content: @Composable () -> Unit,
) {
    val language = derivedStateOf { dependencies.appPreferenceRepository.value.language.getLanguage() }
    LaunchedEffect(language.value) {
        currentLanguage = language.value
    }
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val isDarkMode = derivedStateOf {
        when (dependencies.appPreferenceRepository.value.theme) {
            AppPreference.Theme.System -> isSystemInDarkTheme
            AppPreference.Theme.Light -> false
            AppPreference.Theme.Dark -> true
        }
    }
    CompositionLocalProvider(
        LocalAppContext provides dependencies.context,
        LocalLanguage provides language.value,
        LocalThemeIsDarkMode provides isDarkMode.value,
        LocalAppRecordRepository provides dependencies.appRecordRepository,
        LocalAppPreferenceRepository provides dependencies.appPreferenceRepository,
        LocalAppActionStore provides dependencies.appActionStore,
        LocalToastController provides dependencies.toastController,
        LocalAlertDialogController provides dependencies.alertDialogController,
        LocalProgressController provides dependencies.progressController,
        LocalFileInteractor provides dependencies.fileInteractor,
        LocalPermissionChecker provides dependencies.permissionChecker,
        LocalReclistRepository provides dependencies.reclistRepository,
        LocalSessionRepository provides dependencies.sessionRepository,
    ) {
        content()
    }
}
