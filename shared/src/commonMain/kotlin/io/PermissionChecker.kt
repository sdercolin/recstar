package io

import androidx.compose.runtime.staticCompositionLocalOf
import ui.model.AppContext

/** A helper class to check and request permissions. */
expect class PermissionChecker(appContext: AppContext) {
    /**
     * Checks if the app has the permission to record audio and requests it if necessary. The response of the request
     * made by this call is not handled by this function. To get the result of the request, another call to this
     * function is required.
     */
    fun checkAndRequestRecordingPermission(): Boolean

    /**
     * Checks if the OS has ignored the app's request to record audio. If this returns true, it means the user has
     * denied the permission more some times, or has checked the "Don't ask again" option. In this case, the app should
     * show a dialog explaining why it needs the permission and how to enable it manually.
     */
    fun checkRecordingPermissionIgnored(): Boolean
}

val LocalPermissionChecker = staticCompositionLocalOf<PermissionChecker> {
    error("No PermissionChecker provided")
}
