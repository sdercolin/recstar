package io

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ui.model.AppContext
import ui.model.androidNativeContext

actual class PermissionChecker actual constructor(private val appContext: AppContext) {
    private val activity: Activity get() = appContext.androidNativeContext as Activity

    actual fun checkAndRequestRecordingPermission(): Boolean {
        val permission = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.RECORD_AUDIO,
        )
        return if (permission == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION,
            )
            false
        }
    }

    actual fun checkRecordingPermissionIgnored(): Boolean =
        ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.RECORD_AUDIO,
        )

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 1001
    }
}
