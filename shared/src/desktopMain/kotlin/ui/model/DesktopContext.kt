package ui.model

import io.File
import util.toJavaFile
import java.awt.Desktop

class DesktopContext : AppContext {
    override fun requestOpenFolder(folder: File) {
        Desktop.getDesktop().open(folder.toJavaFile())
    }

    override fun checkAndRequestRecordingPermission(): Boolean = true
}