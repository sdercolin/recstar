package ui.common

import androidx.compose.runtime.Composable
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleCancel
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import ui.model.AppContext
import ui.model.uiViewControllerContext

actual class AlertDialogController actual constructor(private val context: AppContext) {
    actual fun show(request: AlertDialogRequest) {
        val alertController = UIAlertController.alertControllerWithTitle(
            request.title,
            request.message,
            UIAlertControllerStyleAlert,
        )

        alertController.addAction(
            UIAlertAction.actionWithTitle(
                request.confirmButton,
                UIAlertActionStyleDefault,
            ) { _ ->
                request.onConfirm?.invoke()
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
