package ui.common

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.DialogProperties
import ui.model.AppContext

actual class AlertDialogController actual constructor(context: AppContext) {
    private val state = mutableStateOf<AlertDialogRequest?>(null)

    actual fun show(request: AlertDialogRequest) {
        state.value = request
    }

    private fun dismiss() {
        state.value = null
    }

    @Composable
    actual fun Compose() {
        val request = state.value
        if (request != null) {
            val onDismiss = {
                request.onDismiss?.invoke()
                dismiss()
            }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = if (request.title != null) {
                    @Composable {
                        Text(request.title)
                    }
                } else {
                    null
                },
                text = if (request.message != null) {
                    @Composable {
                        Text(request.message)
                    }
                } else {
                    null
                },
                confirmButton = {
                    Button(
                        onClick = {
                            request.onConfirm?.invoke()
                            dismiss()
                        },
                    ) {
                        Text(request.confirmButton)
                    }
                },
                dismissButton = if (request.dismissButton != null) {
                    @Composable {
                        Button(
                            onClick = onDismiss,
                        ) {
                            Text(request.dismissButton)
                        }
                    }
                } else {
                    null
                },
                properties = DialogProperties(
                    dismissOnClickOutside = request.cancelOnClickOutside,
                ),
            )
        }
    }
}
