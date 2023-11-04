package io

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ui.common.AlertDialogController
import ui.common.ToastController
import ui.common.requestConfirmError
import ui.common.show
import ui.string.*
import util.Log
import util.toJavaFile
import java.lang.ref.WeakReference

class ExportDataDelegate(
    activity: AppCompatActivity,
    toastController: ToastController,
    alertDialogController: AlertDialogController,
) {
    private val activityRef = WeakReference(activity)

    private fun onStart() {
        Log.i("Exporting data: ${request?.folder?.absolutePath}")
        request?.onStart?.invoke()
    }

    private val onSuccess = {
        Log.i("Successfully exported data: ${request?.folder?.absolutePath}")
        request?.onSuccess?.invoke()
        toastController.show(stringStatic(Strings.ToastExportDataSuccess))
    }

    private val onCancel = {
        Log.i("Cancelled exporting data: ${request?.folder?.absolutePath}")
        request?.onCancel?.invoke()
        toastController.show(stringStatic(Strings.ToastExportDataCancel))
    }

    private val onError = { t: Throwable ->
        Log.e("Failed to export data", t)
        request?.onError?.invoke(t)
        alertDialogController.requestConfirmError(message = stringStatic(Strings.ErrorExportDataFailedMessage))
    }

    private val exportDataContract: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val scopedActivity = activityRef.get() ?: return@registerForActivityResult
            val request = this.request ?: return@registerForActivityResult
            if (result.resultCode != AppCompatActivity.RESULT_OK) {
                onCancel()
                return@registerForActivityResult
            }
            val treeUri = result.data?.data
            if (treeUri == null) {
                onCancel()
                return@registerForActivityResult
            }
            try {
                val pickedDir =
                    DocumentFile.fromTreeUri(scopedActivity, treeUri)
                if (pickedDir == null) {
                    onCancel()
                } else {
                    onStart()
                    activity.lifecycleScope.launch(Dispatchers.IO) {
                        copyFolderToPublicStorage(pickedDir, request)
                        withContext(Dispatchers.Main) {
                            onSuccess()
                        }
                    }
                }
            } catch (t: Throwable) {
                onError(t)
            }
        }

    private fun copyFolderToPublicStorage(
        pickedDir: DocumentFile,
        request: ExportDataRequest,
    ) {
        val context = activityRef.get() ?: return
        val contentResolver = context.contentResolver

        var targetDir = pickedDir.findFile(request.folder.name)
        targetDir?.delete()
        targetDir = pickedDir.createDirectory(request.folder.name)

        if (targetDir == null) {
            throw RuntimeException("Could not create the directory: ${request.folder.name} under ${pickedDir.uri}")
        }
        val files = if (request.allowedExtension.isNotEmpty()) {
            request.folder.listFiles().filter { file ->
                request.allowedExtension.contains(file.extension)
            }
        } else {
            request.folder.listFiles().filter { it.isFile }
        }

        files.forEach { file ->
            try {
                val newFile = targetDir.createFile(mapExtensionsToMimeType(request.allowedExtension), file.name)
                if (newFile != null) {
                    contentResolver.openOutputStream(newFile.uri).use { outputStream ->
                        if (outputStream != null) {
                            file.toJavaFile().inputStream().use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                    }
                }
            } catch (t: Throwable) {
                throw RuntimeException("Failed to copy file: ${file.name}", t)
            }
        }
    }

    fun launch(request: ExportDataRequest) {
        this.request = request
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        exportDataContract.launch(intent)
    }

    private var request: ExportDataRequest? = null

    private fun mapExtensionsToMimeType(allowedExtensions: List<String>): String =
        when (allowedExtensions.firstOrNull()) {
            "wav" -> "audio/wav"
            else -> "*/*"
        }
}
