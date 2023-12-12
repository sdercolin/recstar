package io

import androidx.compose.runtime.staticCompositionLocalOf
import ui.common.AlertDialogController
import ui.common.ToastController
import ui.model.AppContext

/**
 * A helper class to interact with the file system.
 */
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
     * @param title The title of the file picker dialog, if requested.
     * @param allowedExtensions The allowed file extensions. If empty, all files are allowed.
     * @param onFinish The callback to be called when the dialog is dismissed. The parameter is the picked file, or null
     *     if the user cancelled the dialog.
     * @param initialDirectory The initial directory to be opened in the file picker dialog.
     */
    fun pickFile(
        title: String,
        allowedExtensions: List<String>,
        onFinish: (File?) -> Unit,
        initialDirectory: File? = null,
    )

    /**
     * Starts a file picker dialog to let the user pick a folder to export the data.
     */
    fun exportData(request: ExportDataRequest)
}

val LocalFileInteractor = staticCompositionLocalOf<FileInteractor> {
    error("No FileInteractor provided")
}

/**
 * A request to export the data.
 *
 * @param folder The folder which contains the data to be exported.
 * @param allowedExtension The allowed file extensions. If empty, all files are allowed.
 * @param onStart The callback to be called when the export starts.
 * @param onSuccess The callback to be called when the export is successful.
 * @param onCancel The callback to be called when the export is cancelled.
 * @param onError The callback to be called when the export fails.
 */
class ExportDataRequest(
    val folder: File,
    val allowedExtension: List<String>,
    val onStart: (() -> Unit)? = null,
    val onSuccess: (() -> Unit)? = null,
    val onCancel: (() -> Unit)? = null,
    val onError: ((Throwable) -> Unit)? = null,
)
