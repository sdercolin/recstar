package ui.model

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope

/**
 * This interface is used to provide platform specific functionality to the shared code.
 * - On Android, this is implemented by the single Activity of the app.
 * - On iOS, this is implemented by the top-level ViewController.
 * - On Desktop, this is implemented by Java's Desktop API.
 */
interface AppContext {
    /** The app-level [CoroutineScope]. */
    val coroutineScope: CoroutineScope
}

val LocalAppContext = staticCompositionLocalOf<AppContext> {
    error("No Context provided!")
}
