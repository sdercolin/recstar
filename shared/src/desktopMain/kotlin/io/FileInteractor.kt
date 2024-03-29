package io

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ui.common.AlertDialogController
import ui.common.FileDialogResult
import ui.common.OpenFileDialogRequest
import ui.common.ToastController
import ui.model.AppContext
import util.JavaFile
import util.Log
import util.toFile
import util.toJavaFile
import java.awt.Desktop

actual class FileInteractor actual constructor(
    context: AppContext,
    toastController: ToastController,
    alertDialogController: AlertDialogController,
) {
    actual fun requestOpenFolder(folder: File) {
        if (folder.isDirectory) {
            Desktop.getDesktop().open(folder.toJavaFile())
        } else {
            Log.e("FileInteractor.requestOpenFolder: ${folder.absolutePath} is not a directory")
        }
    }

    var fileDialogRequest: FileDialogResult? by mutableStateOf(null)
        private set

    actual fun pickFile(
        title: String,
        allowedExtensions: List<String>,
        onFinish: (File?) -> Unit,
        initialDirectory: File?,
    ) {
        fileDialogRequest = OpenFileDialogRequest(
            title = title,
            extensions = allowedExtensions,
            initialDirectory = initialDirectory?.absolutePath,
            directoryMode = allowedExtensions.singleOrNull() == "",
            onCloseRequest = { parent, name ->
                if (parent == null || name == null) {
                    fileDialogRequest = null
                    onFinish(null)
                    return@OpenFileDialogRequest
                }
                val file = JavaFile(parent, name)
                fileDialogRequest = null
                onFinish(file.toFile())
            },
        )
    }

    actual fun exportData(request: ExportDataRequest) {
        // on desktop, we can directly open the folder by the file manager
        // and let the user copy the files manually
    }
}
