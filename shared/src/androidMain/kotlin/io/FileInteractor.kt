package io

import androidx.appcompat.app.AppCompatActivity
import ui.common.AlertDialogController
import ui.common.ToastController
import ui.common.ToastDuration
import ui.common.show
import ui.model.AppContext
import ui.model.androidNativeContext

actual class FileInteractor actual constructor(
    context: AppContext,
    private val toastController: ToastController,
    alertDialogController: AlertDialogController,
) {
    private val activity = context.androidNativeContext as AppCompatActivity

    private val pickFileDelegate = PickFileDelegate(
        activity,
        alertDialogController,
    )

    private val exportDataDelegate = ExportDataDelegate(
        activity,
        toastController,
        alertDialogController,
    )

    actual fun requestOpenFolder(folder: File) {
        toastController.show("Not supported on Android: path=${folder.absolutePath}", ToastDuration.Long)
    }

    actual fun pickFile(
        title: String,
        allowedExtensions: List<String>,
        onFinish: (File?) -> Unit,
    ) = pickFileDelegate.launch(allowedExtensions, onFinish)

    actual fun exportData(request: ExportDataRequest) = exportDataDelegate.launch(request)
}
