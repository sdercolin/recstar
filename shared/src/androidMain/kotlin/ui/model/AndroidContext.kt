package ui.model

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.File
import java.lang.ref.WeakReference

class AndroidContext(private val contextRef: WeakReference<android.content.Context>) : AppContext {

    fun getAndroidNativeContext(): android.content.Context? = contextRef.get()

    override fun requestOpenFolder(folder: File) {
        val context = contextRef.get() ?: return

        // TODO: show alert dialog
    }

    override fun checkAndRequestRecordingPermission(): Boolean {
        val context = contextRef.get()
        if (context !is Activity) {
            return false
        }
        val permission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        )
        return if (permission == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
            false
        }
    }
}

private const val REQUEST_RECORD_AUDIO_PERMISSION = 1001