package ui.common

import androidx.compose.runtime.Composable
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleCancel
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UITextField
import ui.model.AppContext
import ui.model.uiViewControllerContext

actual class AlertDialogController actual constructor(private val context: AppContext) {
    actual fun show(request: AlertDialogRequest) {
        val alertController = UIAlertController.alertControllerWithTitle(
            request.title,
            request.message,
            UIAlertControllerStyleAlert,
        )

        var inputField: UITextField? = null
        if (request.textInput != null) {
            alertController.addTextFieldWithConfigurationHandler { textField ->
                textField ?: return@addTextFieldWithConfigurationHandler
                textField.text = request.textInput.initialValue
                request.textInput.label?.let { label ->
                    textField.placeholder = label
                }
                if (request.textInput.selected) {
                    textField.selectAll(null)
                }
                inputField = textField
            }
        }

        alertController.addAction(
            UIAlertAction.actionWithTitle(
                request.confirmButton,
                UIAlertActionStyleDefault,
            ) { _ ->
                request.onConfirm?.invoke()
                inputField?.text?.let { text ->
                    request.textInput?.onConfirmInput?.invoke(text)
                }
            },
        )

        if (request.dismissButton != null) {
            alertController.addAction(
                UIAlertAction.actionWithTitle(
                    request.dismissButton,
                    UIAlertActionStyleCancel,
                ) { _ ->
                    request.onDismiss?.invoke()
                },
            )
        }

        context.uiViewControllerContext.uiViewController.presentModalViewController(alertController, true)
    }

    @Composable
    actual fun Compose() {
        // keep empty, because we are using iOS's native Alert
    }
}
