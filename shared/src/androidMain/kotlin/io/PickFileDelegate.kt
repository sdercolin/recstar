package io

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import ui.common.AlertDialogController
import ui.common.requestConfirmError
import ui.string.*
import util.Log
import util.toJavaFile
import java.lang.ref.WeakReference

class PickFileDelegate(activity: AppCompatActivity, private val alertDialogController: AlertDialogController) {
    private val activityRef = WeakReference(activity)

    private fun onError(t: Throwable) {
        Log.e("Failed to load file", t)
        alertDialogController.requestConfirmError(message = stringStatic(Strings.ErrorReadFileFailedMessage))
    }

    private val pickFileContract: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val scopedActivity = activityRef.get() ?: return@registerForActivityResult
            val contentResolver = scopedActivity.contentResolver
            if (result.resultCode != Activity.RESULT_OK) {
                pickFileCallback?.invoke(null)
                return@registerForActivityResult
            }
            val uri = result.data?.data
            if (uri == null) {
                pickFileCallback?.invoke(null)
                return@registerForActivityResult
            }
            try {
                val fileName = getFileNameFromContentUri(scopedActivity, uri) ?: return@registerForActivityResult
                val tempFile = Paths.cacheRoot.resolve(fileName)
                if (tempFile.exists()) {
                    tempFile.delete()
                }
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    tempFile.toJavaFile().outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                pickFileCallback?.invoke(Paths.cacheRoot.resolve(fileName))
            } catch (t: Throwable) {
                onError(t)
            }
        }

    private var pickFileCallback: ((File?) -> Unit)? = null

    private fun getFileNameFromContentUri(
        activity: Activity,
        contentUri: Uri,
    ): String? {
        var fileName: String? = null
        activity.contentResolver.query(contentUri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName
    }

    fun launch(
        allowedExtensions: List<String>,
        onFinish: (File?) -> Unit,
    ) {
        pickFileCallback = onFinish
        val (type, mimeTypes) = mapExtensionsToMimeType(allowedExtensions)
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            this.type = type
            action = Intent.ACTION_GET_CONTENT
            mimeTypes?.let { putExtra(Intent.EXTRA_MIME_TYPES, it.toTypedArray()) }
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        }
        pickFileContract.launch(intent)
    }

    private fun mapExtensionsToMimeType(allowedExtensions: List<String>): Pair<String, List<String>?> =
        when (allowedExtensions.firstOrNull()) {
            "txt" -> "text/plain" to null
            "wav" -> "audio/*" to listOf("audio/wav", "audio/x-wav")
            else -> "*/*" to null
        }
}
