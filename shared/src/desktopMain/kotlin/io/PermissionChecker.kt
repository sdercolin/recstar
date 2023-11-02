package io

import ui.model.AppContext

actual class PermissionChecker actual constructor(appContext: AppContext) {
    actual fun checkAndRequestRecordingPermission(): Boolean = true

    actual fun checkRecordingPermissionIgnored(): Boolean = false
}
