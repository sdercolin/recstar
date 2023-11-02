package ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.AwtWindow
import io.Paths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.nfd.NativeFileDialog
import util.isMacOS
import util.isWindows
import util.toJavaFile
import java.awt.FileDialog
import java.awt.FileDialog.LOAD
import java.awt.FileDialog.SAVE
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter

sealed class FileDialogResult

data class OpenFileDialogRequest(
    val title: String,
    val initialDirectory: String? = null,
    val initialFileName: String? = null,
    val extensions: List<String>? = null,
    val directoryMode: Boolean = false,
    val onCloseRequest: (parent: String?, name: String?) -> Unit,
) : FileDialogResult()

data class SaveFileDialogRequest(
    val title: String,
    val initialDirectory: String? = null,
    val initialFileName: String? = null,
    val extensions: List<String>? = null,
    val onCloseRequest: (parent: String?, name: String?) -> Unit,
) : FileDialogResult()

@Composable
fun OpenFileDialog(request: OpenFileDialogRequest) =
    FileDialog(
        LOAD,
        request.title,
        request.initialDirectory,
        request.initialFileName,
        request.extensions,
        request.directoryMode,
        request.onCloseRequest,
    )

@Composable
fun SaveFileDialog(request: SaveFileDialogRequest) =
    FileDialog(
        SAVE,
        request.title,
        request.initialDirectory,
        request.initialFileName,
        request.extensions,
        false,
        request.onCloseRequest,
    )

@Composable
private fun FileDialog(
    mode: Int,
    title: String,
    initialDirectory: String?,
    initialFileName: String?,
    extensions: List<String>?,
    directoryMode: Boolean,
    onCloseRequest: (parent: String?, name: String?) -> Unit,
) = when {
    !isWindows -> AwtWindow(
        create = {
            if (directoryMode) setAwtDirectoryMode(true)

            object : FileDialog(null as Frame?, title, mode) {
                override fun setVisible(value: Boolean) {
                    super.setVisible(value)
                    if (value) {
                        onCloseRequest(directory, file)
                    }
                }
            }.apply {
                initialDirectory?.let { directory = it }
                initialFileName?.let { file = it }
                if (!extensions.isNullOrEmpty()) {
                    filenameFilter = FilenameFilter { _, name ->
                        extensions.any {
                            name.endsWith(it)
                        }
                    }
                }
            }
        },
        dispose = {
            if (directoryMode) setAwtDirectoryMode(false)
            it.dispose()
        },
    )
    else -> LwjglFileDialog(
        mode,
        initialDirectory,
        initialFileName,
        extensions,
        directoryMode,
        onCloseRequest,
    )
}

@Composable
private fun LwjglFileDialog(
    mode: Int,
    initialDirectory: String?,
    initialFileName: String?,
    extensions: List<String>?,
    directoryMode: Boolean,
    onCloseRequest: (parent: String?, name: String?) -> Unit,
) {
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pathPointer = MemoryUtil.memAllocPointer(1)

            val filterList = extensions?.joinToString(",")
            val defaultPathForFile = if (initialDirectory != null && initialFileName != null) {
                File(initialDirectory, initialFileName).absolutePath
            } else {
                null
            }
            val result = when {
                mode == SAVE -> {
                    NativeFileDialog.NFD_SaveDialog(filterList, defaultPathForFile, pathPointer)
                }

                directoryMode -> {
                    NativeFileDialog.NFD_PickFolder(initialDirectory ?: Paths.home.absolutePath, pathPointer)
                }

                else -> {
                    NativeFileDialog.NFD_OpenDialog(filterList, defaultPathForFile, pathPointer)
                }
            }
            if (result == NativeFileDialog.NFD_OKAY) {
                val file = pathPointer.stringUTF8.toJavaFile()
                NativeFileDialog.nNFD_Free(pathPointer[0])
                onCloseRequest(file.parent, file.name)
            } else {
                onCloseRequest(null, null)
            }
            MemoryUtil.memFree(pathPointer)
        }
    }
}

/**
 * Sets directory mode for the Awt file dialog.
 */
private fun setAwtDirectoryMode(on: Boolean) {
    if (isMacOS) {
        System.setProperty("apple.awt.fileDialogForDirectories", on.toString())
    }
}
