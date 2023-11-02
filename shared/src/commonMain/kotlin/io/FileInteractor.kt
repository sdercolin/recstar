package io

import androidx.compose.runtime.staticCompositionLocalOf
import ui.common.AlertDialogController
import ui.common.ToastController
import ui.model.AppContext

/** A helper class to interact with the file system. */
expect class FileInteractor(
    context: AppContext,
    toastController: ToastController,
    alertDialogController: AlertDialogController,
) {
    /**
     * Opens the given folder in the platform's default file manager.
     * - On Android, this is currently not supported.
     */
    fun requestOpenFolder(folder: File)

    /**
     * Starts a file picker dialog to let the user pick a file.
     *
     * @param title The title of the file picker dialog.
     * @param allowedExtensions The allowed file extensions. If empty, all files are allowed.
     * @param onFinish The callback to be called when the dialog is dismissed. The parameter is the picked file, or null
     *     if the user cancelled the dialog.
     */
    fun pickFile(
        title: String,
        allowedExtensions: List<String>,
        onFinish: (File?) -> Unit,
    )
}

val LocalFileInteractor = staticCompositionLocalOf<FileInteractor> {
    error("No FileInteractor provided")
}
