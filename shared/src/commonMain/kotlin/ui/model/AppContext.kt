package ui.model

import androidx.compose.runtime.staticCompositionLocalOf
import io.File
import kotlinx.coroutines.CoroutineScope
import repository.ReclistRepository

/**
 * This interface is used to provide platform specific functionality to the shared code.
 * - On Android, this is implemented by the single Activity of the app.
 * - On iOS, this is implemented by the top-level ViewController.
 * - On Desktop, this is implemented by Java's Desktop API.
 */
interface AppContext {
    /** The [ReclistRepository] instance to manage reclist files. */
    val reclistRepository: ReclistRepository

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

    /**
     * Checks if the app has the permission to record audio and requests it if necessary. The response of the request
     * made by this call is not handled by this function. To get the result of the request, another call to this
     * function is required.
     */
    fun checkAndRequestRecordingPermission(): Boolean

    /**
     * Checks if the OS has ignored the app's request to record audio. If this returns true, it means the user has
     * denied the permission more some times, or has checked the "Don't ask again" option. In this case, the app should
     * show a dialog explaining why it needs the permission and how to enable it manually.
     */
    fun checkRecordingPermissionIgnored(): Boolean

    /** The app-level [CoroutineScope]. */
    val coroutineScope: CoroutineScope
}

val LocalAppContext = staticCompositionLocalOf<AppContext> {
    error("No Context provided!")
}
