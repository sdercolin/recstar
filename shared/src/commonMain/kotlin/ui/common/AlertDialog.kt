package ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import ui.model.AppContext

/** An app-wide controller for showing alert dialogs. */
expect class AlertDialogController(context: AppContext) {

    fun show(request: AlertDialogRequest)

    @Composable
    fun Compose()
}

val LocalAlertDialogController = staticCompositionLocalOf<AlertDialogController> {
    error("No AlertDialogController provided!")
}

/**
 * A request to show an alert dialog.
 *
 * @property title The title of the dialog.
 * @property message The message of the dialog.
 * @property confirmButton The text of the confirm button.
 * @property dismissButton The text of the dismiss button. If null, the dialog will not have a dismiss button.
 * @property onConfirm The callback to be invoked when the confirm button is clicked.
 * @property onDismiss The callback to be invoked when the dismiss button is clicked, or when the dialog is dismissed by
 *     clicking outside / back button etc.
 * @property cancelOnClickOutside Whether the dialog should be dismissed when clicking outside. This is not effective on
 *     iOS.
 */
class AlertDialogRequest(
    val title: String? = null,
    val message: String? = null,
    val confirmButton: String,
    val dismissButton: String? = null,
    val onConfirm: (() -> Unit)? = null,
    val onDismiss: (() -> Unit)? = null,
    val cancelOnClickOutside: Boolean = true,
)

fun AlertDialogController.requestConfirm(
    title: String? = null,
    message: String? = null,
    confirmButton: String = "OK",
    onFinish: (() -> Unit)? = null,
) = AlertDialogRequest(
    title = title,
    message = message,
    confirmButton = confirmButton,
    onConfirm = onFinish,
    onDismiss = onFinish,
).let { show(it) }

fun AlertDialogController.requestConfirmCancellable(
    title: String? = null,
    message: String? = null,
    confirmButton: String = "OK",
    dismissButton: String = "Cancel",
    onConfirm: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
) = AlertDialogRequest(
    title = title,
    message = message,
    confirmButton = confirmButton,
    dismissButton = dismissButton,
    onConfirm = onConfirm,
    onDismiss = onDismiss,
).let { show(it) }

fun AlertDialogController.requestYesNo(
    title: String? = null,
    message: String? = null,
    confirmButton: String = "Yes",
    dismissButton: String = "No",
    onConfirm: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    cancelOnClickOutside: Boolean = false,
) = AlertDialogRequest(
    title = title,
    message = message,
    confirmButton = confirmButton,
    dismissButton = dismissButton,
    onConfirm = onConfirm,
    onDismiss = onDismiss,
    cancelOnClickOutside = cancelOnClickOutside,
).let { show(it) }
