import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import io.Paths
import ui.common.AlertDialogController
import ui.common.LocalAlertDialogController
import ui.common.LocalToastController
import ui.common.ToastController
import ui.demo.AlertDemo
import ui.model.AppContext
import ui.model.LocalAppContext
import util.Log

@Composable
fun App(context: AppContext) {
    remember { ensurePaths() }

    val alertDialogController = remember(context) { AlertDialogController(context) }
    val toastController = remember(context) { ToastController(context) }

    CompositionLocalProvider(
        LocalAppContext provides context,
        LocalAlertDialogController provides alertDialogController,
        LocalToastController provides toastController,
    ) {
        MaterialTheme {
            Surface {
                AlertDemo()
                alertDialogController.Compose()
                toastController.Compose()
            }
        }
    }
}

private fun ensurePaths() {
    listOf(Paths.appRoot, Paths.contentRoot).forEach {
        Log.i("ensurePaths: ${it.absolutePath}")
        if (!it.exists()) {
            Log.i("not exists, creating...")
            val created = it.mkdirs()
            Log.i("created: $created")
        }
    }
}
