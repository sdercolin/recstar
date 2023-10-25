package ui.common

import androidx.compose.runtime.Composable
import kotlinx.coroutines.launch
import ui.model.AppContext

actual class ToastController actual constructor(private val context: AppContext) {
    private val snackbarBasedToastController = SnackbarBasedToastController()

    actual fun show(request: ToastRequest) {
        context.coroutineScope.launch {
            snackbarBasedToastController.show(request)
        }
    }

    @Composable
    actual fun Compose() {
        snackbarBasedToastController.Compose()
    }
}
