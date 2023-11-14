import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import io.FileInteractor
import io.LocalFileInteractor
import io.LocalPermissionChecker
import io.PermissionChecker
import repository.AppActionStore
import repository.AppRecordRepository
import repository.LocalAppActionStore
import repository.LocalAppRecordRepository
import repository.LocalReclistRepository
import repository.LocalSessionRepository
import repository.ReclistRepository
import repository.SessionRepository
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
import util.Locale
import util.Log
import util.isDebug

/**
 * A class to hold all static dependencies of the app, bound to the lifecycle of the [context].
 */
class AppDependencies(
    val context: AppContext,
    val language: Language = findBestMatchedLanguage(),
    val appRecordRepository: AppRecordRepository = createAppRecordRepository(context.coroutineScope),
    val appActionStore: AppActionStore = AppActionStore(context.coroutineScope),
    val toastController: ToastController = ToastController(context),
    val alertDialogController: AlertDialogController = AlertDialogController(context),
    val progressController: ProgressController = ProgressController(),
    val fileInteractor: FileInteractor = FileInteractor(context, toastController, alertDialogController),
    val permissionChecker: PermissionChecker = PermissionChecker(context),
    val reclistRepository: ReclistRepository = ReclistRepository(),
    val sessionRepository: SessionRepository = SessionRepository(reclistRepository),
)

private fun findBestMatchedLanguage(): Language {
    if (isDebug) {
        // before UI to select languages is implemented, we use English for debug builds
        return Language.default
    }
    val detected = Language.find(Locale)
    Log.i("Locale: $Locale, Language: $detected")
    return detected ?: Language.default
}

@Composable
fun ProvideAppDependencies(
    dependencies: AppDependencies,
    content: @Composable () -> Unit,
) {
    LaunchedEffect(dependencies.language) {
        currentLanguage = dependencies.language
    }
    CompositionLocalProvider(
        LocalAppContext provides dependencies.context,
        LocalLanguage provides dependencies.language,
        LocalAppRecordRepository provides dependencies.appRecordRepository,
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
