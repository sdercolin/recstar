package ui.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.File
import kotlinx.coroutines.CoroutineScope
import repository.ReclistRepository
import ui.common.AlertDialogController
import ui.common.FileDialogResult
import ui.common.OpenFileDialogRequest
import ui.common.ToastController
import util.JavaFile
import util.toFile
import util.toJavaFile
import java.awt.Desktop

class DesktopContext(override val coroutineScope: CoroutineScope) : AppContext {
    override val reclistRepository: ReclistRepository = ReclistRepository(this)

    override val toastController: ToastController = ToastController(this)

    override val alertDialogController: AlertDialogController = AlertDialogController(this)

    override fun requestOpenFolder(folder: File) {
        Desktop.getDesktop().open(folder.toJavaFile())
    }

    var fileDialogRequest: FileDialogResult? by mutableStateOf(null)
        private set

    override fun pickFile(
        title: String,
        allowedExtensions: List<String>,
        onFinish: (File?) -> Unit,
    ) {
        fileDialogRequest = OpenFileDialogRequest(
            title = title,
            extensions = allowedExtensions,
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

    override fun checkAndRequestRecordingPermission(): Boolean = true

    override fun checkRecordingPermissionIgnored(): Boolean = false
}
