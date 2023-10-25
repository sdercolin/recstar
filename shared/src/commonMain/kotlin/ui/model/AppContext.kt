package ui.model

import androidx.compose.runtime.staticCompositionLocalOf
import io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * This interface is used to provide platform specific functionality to the shared code.
 * - On Android, this is implemented by the single Activity of the app.
 * - On iOS, this is implemented by the top-level ViewController.
 * - On Desktop, this is implemented by Java's Desktop API.
 */
interface AppContext {
    /**
     * Opens the given folder in the platform's default file manager.
     * - On Android, this is currently not supported.
     */
    fun requestOpenFolder(folder: File)

    /**
     * Checks if the app has the permission to record audio and requests it if necessary. The response of the request
     * made by this call is not handled by this function. To get the result of the request, another call to this
     * function is required.
     */
    fun checkAndRequestRecordingPermission(): Boolean

    /** The app-level [CoroutineScope]. */
    val coroutineScope: CoroutineScope

    /** A [Flow] that emits a value when popping the current screen is requested from the platform. */
    val navigationPopEvent: Flow<Unit> get() = emptyFlow()
}

val LocalAppContext = staticCompositionLocalOf<AppContext> {
    error("No Context provided!")
}
