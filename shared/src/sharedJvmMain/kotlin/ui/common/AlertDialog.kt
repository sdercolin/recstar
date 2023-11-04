package ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
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
            var textInputValue by mutableStateOf(
                if (request.textInput == null) {
                    TextFieldValue()
                } else {
                    TextFieldValue(
                        text = request.textInput.initialValue,
                        selection = if (request.textInput.selected) {
                            TextRange(0, request.textInput.initialValue.length)
                        } else {
                            TextRange.Zero
                        },
                    )
                },
            )
            AlertDialog(
                onDismissRequest = onDismiss,
                title = if (request.title != null) {
                    @Composable {
                        Text(request.title)
                    }
                } else {
                    null
                },
                text = if (request.message != null || request.textInput != null) {
                    @Composable {
                        Column {
                            if (request.message != null) {
                                Text(request.message)
                            }
                            if (request.textInput != null) {
                                val focusRequester = remember { FocusRequester() }
                                LaunchedEffect(Unit) {
                                    focusRequester.requestFocus()
                                }
                                // Spacer or paddings don't work here, so we use an empty text as a workaround.
                                Text("", modifier = Modifier.height(8.dp))
                                TextField(
                                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                                    value = textInputValue,
                                    onValueChange = { textInputValue = it },
                                    label = {
                                        if (request.textInput.label != null) {
                                            Text(request.textInput.label)
                                        }
                                    },
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                } else {
                    null
                },
                confirmButton = {
                    Button(
                        onClick = {
                            request.onConfirm?.invoke()
                            request.textInput?.onConfirmInput?.invoke(textInputValue.text)
                            dismiss()
                        },
                    ) {
                        Text(request.confirmButton)
                    }
                },
                dismissButton = if (request.dismissButton != null) {
                    @Composable {
                        TextButton(
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
