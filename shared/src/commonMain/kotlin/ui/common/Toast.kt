package ui.common

import androidx.compose.runtime.Composable
import ui.model.AppContext

/**
 * An app-wide controller for showing toasts.
 */
expect class ToastController(context: AppContext) {
    fun show(request: ToastRequest)

    @Composable
    fun Compose()
}

val LocalToastController = androidx.compose.runtime.staticCompositionLocalOf<ToastController> {
    error("No ToastController provided!")
}

/**
 * A request to show a toast.
 *
 * @property message The message of the toast.
 * @property duration The duration of the toast, defaults to [ToastDuration.Short].
 */
class ToastRequest(
    val message: String,
    val duration: ToastDuration = ToastDuration.Short,
)

enum class ToastDuration {
    Short,
    Long,
}

fun ToastController.show(
    message: String,
    duration: ToastDuration = ToastDuration.Short,
) = show(ToastRequest(message, duration))
