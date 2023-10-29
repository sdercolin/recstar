package ui.model

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.File
import kotlinx.coroutines.CoroutineScope
import ui.common.ToastController
import ui.common.ToastDuration
import ui.common.show
import java.lang.ref.WeakReference

class AndroidContext(
    private val contextRef: WeakReference<android.content.Context>,
    override val coroutineScope: CoroutineScope,
) : AppContext {
    fun getAndroidNativeContext(): android.content.Context? = contextRef.get()

    var toastController: ToastController? = null

    override fun requestOpenFolder(folder: File) {
        toastController?.show("Not supported on Android: path=${folder.absolutePath}", ToastDuration.Long)
    }

    override fun checkAndRequestRecordingPermission(): Boolean {
        val context = contextRef.get()
        if (context !is Activity) {
            return false
        }
        val permission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        )
        return if (permission == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION,
            )
            false
        }
    }

    override fun checkRecordingPermissionIgnored(): Boolean {
        val context = contextRef.get()
        if (context !is Activity) {
            return false
        }
        return ActivityCompat.shouldShowRequestPermissionRationale(
            context,
            Manifest.permission.RECORD_AUDIO,
        )
    }
}

private const val REQUEST_RECORD_AUDIO_PERMISSION = 1001

val AppContext.androidContext: AndroidContext
    get() = this as AndroidContext

val AppContext.androidNativeContext: android.content.Context
    get() = requireNotNull(androidContext.getAndroidNativeContext())
