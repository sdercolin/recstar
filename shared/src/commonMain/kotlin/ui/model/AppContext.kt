package ui.model

import androidx.compose.runtime.staticCompositionLocalOf
import io.File

interface AppContext {

    fun requestOpenFolder(folder: File)
    fun checkAndRequestRecordingPermission(): Boolean
}

val LocalAppContext = staticCompositionLocalOf<AppContext> {
    error("No Context provided!")
}