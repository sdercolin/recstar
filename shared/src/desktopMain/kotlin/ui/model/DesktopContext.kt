package ui.model

import io.File
import kotlinx.coroutines.CoroutineScope
import util.toJavaFile
import java.awt.Desktop

class DesktopContext(override val coroutineScope: CoroutineScope) : AppContext {
    override fun requestOpenFolder(folder: File) {
        Desktop.getDesktop().open(folder.toJavaFile())
    }

    override fun checkAndRequestRecordingPermission(): Boolean = true

    override fun checkRecordingPermissionIgnored(): Boolean = false
}
