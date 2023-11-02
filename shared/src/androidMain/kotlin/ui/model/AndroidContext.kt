package ui.model

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.File
import io.PickFileDelegate
import kotlinx.coroutines.CoroutineScope
import repository.ReclistRepository
import ui.common.AlertDialogController
import ui.common.ToastController
import ui.common.ToastDuration
import ui.common.requestConfirm
import ui.common.show
import ui.string.*
import util.Log
import java.lang.ref.WeakReference

class AndroidContext(
    activity: AppCompatActivity,
    override val coroutineScope: CoroutineScope,
) : AppContext {
    private val activityRef = WeakReference(activity)

    fun getAndroidNativeContext(): android.content.Context? = activityRef.get()

    override val toastController = ToastController(this)

    override val alertDialogController = AlertDialogController(this)

    override val reclistRepository: ReclistRepository = ReclistRepository(this)

    private val pickFileDelegate = PickFileDelegate(
        activity,
        onError = {
            Log.e("Failed to load file", it)
            alertDialogController.requestConfirm(message = stringStatic(Strings.ErrorReadFileFailedMessage))
        },
    )

    override fun requestOpenFolder(folder: File) {
        toastController.show("Not supported on Android: path=${folder.absolutePath}", ToastDuration.Long)
    }

    override fun pickFile(
        title: String,
        allowedExtensions: List<String>,
        onFinish: (File?) -> Unit,
    ) = pickFileDelegate.launch(allowedExtensions, onFinish)

    override fun checkAndRequestRecordingPermission(): Boolean {
        val context = activityRef.get()
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
        val context = activityRef.get()
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
private const val REQUEST_CODE_PICK_FILE = 1002

val AppContext.androidContext: AndroidContext
    get() = this as AndroidContext

val AppContext.androidNativeContext: android.content.Context
    get() = requireNotNull(androidContext.getAndroidNativeContext())
