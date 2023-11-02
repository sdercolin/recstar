package io

import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import ui.model.AppContext

actual class PermissionChecker actual constructor(appContext: AppContext) {
    actual fun checkAndRequestRecordingPermission(): Boolean {
        val authStatus = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeAudio)

        when (authStatus) {
            AVAuthorizationStatusNotDetermined -> {
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeAudio) { granted ->
                    // You can handle the response here if needed.
                    // For example, update UI or state based on whether 'granted' is true or false.
                }
            }
            AVAuthorizationStatusAuthorized -> {
                // Already authorized.
            }
            AVAuthorizationStatusDenied, AVAuthorizationStatusRestricted -> {
                // Access denied or restricted. Handle accordingly.
            }
        }

        return authStatus == AVAuthorizationStatusAuthorized
    }

    actual fun checkRecordingPermissionIgnored(): Boolean {
        val authStatus = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeAudio)
        return authStatus == AVAuthorizationStatusDenied || authStatus == AVAuthorizationStatusRestricted
    }
}
