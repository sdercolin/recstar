package io

import androidx.appcompat.app.AppCompatActivity
import ui.common.AlertDialogController
import ui.common.ToastController
import ui.common.ToastDuration
import ui.common.requestConfirm
import ui.common.show
import ui.model.AppContext
import ui.model.androidNativeContext
import ui.string.*
import util.Log

actual class FileInteractor actual constructor(
    context: AppContext,
    private val toastController: ToastController,
    private val alertDialogController: AlertDialogController,
) {
    private val activity = context.androidNativeContext as AppCompatActivity

    private val pickFileDelegate = PickFileDelegate(
        activity,
        onError = {
            Log.e("Failed to load file", it)
            alertDialogController.requestConfirm(message = stringStatic(Strings.ErrorReadFileFailedMessage))
        },
    )

    actual fun requestOpenFolder(folder: File) {
        toastController.show("Not supported on Android: path=${folder.absolutePath}", ToastDuration.Long)
    }

    actual fun pickFile(
        title: String,
        allowedExtensions: List<String>,
        onFinish: (File?) -> Unit,
    ) = pickFileDelegate.launch(allowedExtensions, onFinish)
}
