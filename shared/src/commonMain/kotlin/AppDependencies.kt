import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import io.FileInteractor
import io.LocalFileInteractor
import io.LocalPermissionChecker
import io.PermissionChecker
import repository.LocalReclistRepository
import repository.LocalSessionRepository
import repository.ReclistRepository
import repository.SessionRepository
import ui.common.AlertDialogController
import ui.common.LocalAlertDialogController
import ui.common.LocalToastController
import ui.common.ToastController
import ui.model.AppContext
import ui.model.LocalAppContext

/**
 * A class to hold all static dependencies of the app, bound to the lifecycle of the [context].
 */
class AppDependencies(
    val context: AppContext,
    val toastController: ToastController = ToastController(context),
    val alertDialogController: AlertDialogController = AlertDialogController(context),
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
    CompositionLocalProvider(
        LocalAppContext provides dependencies.context,
        LocalToastController provides dependencies.toastController,
        LocalAlertDialogController provides dependencies.alertDialogController,
        LocalFileInteractor provides dependencies.fileInteractor,
        LocalPermissionChecker provides dependencies.permissionChecker,
        LocalReclistRepository provides dependencies.reclistRepository,
        LocalSessionRepository provides dependencies.sessionRepository,
    ) {
        content()
    }
}
