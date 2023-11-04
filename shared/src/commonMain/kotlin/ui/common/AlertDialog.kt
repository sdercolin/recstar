package ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import ui.model.AppContext
import ui.string.*

/**
 * An app-wide controller for showing alert dialogs.
 */
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
 * @property textInput The text input to be shown in the dialog. If null, the dialog will not have a text input.
 */
class AlertDialogRequest(
    val title: String? = null,
    val message: String? = null,
    val confirmButton: String,
    val dismissButton: String? = null,
    val onConfirm: (() -> Unit)? = null,
    val onDismiss: (() -> Unit)? = null,
    val cancelOnClickOutside: Boolean = true,
    val textInput: TextInput? = null,
) {
    /**
     * A text input to be shown in the dialog.
     *
     * @param label The label of the text input.
     * @param initialValue The initial value of the text input.
     * @param selected Whether the text input should be selected when the dialog is shown.
     * @param onConfirmInput The callback to be invoked when the confirm button is clicked.
     */
    data class TextInput(
        val label: String? = null,
        val initialValue: String = "",
        val selected: Boolean = false,
        val onConfirmInput: (String) -> Unit,
    )
}

fun AlertDialogController.requestConfirm(
    title: String? = null,
    message: String? = null,
    confirmButton: String = stringStatic(Strings.CommonOkay),
    onFinish: (() -> Unit)? = null,
) = AlertDialogRequest(
    title = title,
    message = message,
    confirmButton = confirmButton,
    onConfirm = onFinish,
    onDismiss = onFinish,
).let { show(it) }

fun AlertDialogController.requestConfirmError(
    message: String? = null,
    confirmButton: String = stringStatic(Strings.CommonOkay),
    onFinish: (() -> Unit)? = null,
) = requestConfirm(
    title = stringStatic(Strings.CommonError),
    message = message,
    confirmButton = confirmButton,
    onFinish = onFinish,
)

fun AlertDialogController.requestConfirmCancellable(
    title: String? = null,
    message: String? = null,
    confirmButton: String = stringStatic(Strings.CommonOkay),
    dismissButton: String = stringStatic(Strings.CommonCancel),
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
    confirmButton: String = stringStatic(Strings.CommonYes),
    dismissButton: String = stringStatic(Strings.CommonNo),
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

fun AlertDialogController.requestInput(
    title: String? = null,
    message: String? = null,
    confirmButton: String = stringStatic(Strings.CommonOkay),
    dismissButton: String = stringStatic(Strings.CommonCancel),
    onConfirm: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    cancelOnClickOutside: Boolean = false,
    label: String? = null,
    initialValue: String = "",
    selected: Boolean = false,
    onConfirmInput: (String) -> Unit,
) = AlertDialogRequest(
    title = title,
    message = message,
    confirmButton = confirmButton,
    dismissButton = dismissButton,
    onConfirm = onConfirm,
    onDismiss = onDismiss,
    cancelOnClickOutside = cancelOnClickOutside,
    textInput = AlertDialogRequest.TextInput(
        label = label,
        initialValue = initialValue,
        selected = selected,
        onConfirmInput = onConfirmInput,
    ),
).let { show(it) }
