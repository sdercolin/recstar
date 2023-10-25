package ui.model

import io.File
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController

class ViewControllerContext(val uiViewController: UIViewController) : AppContext {
    override fun requestOpenFolder(folder: File) {
        val folderURL = folder.toNSURL()
        val documentPicker = UIDocumentPickerViewController(
            documentTypes = listOf("public.folder"),
            inMode = UIDocumentPickerMode.UIDocumentPickerModeOpen,
        ).apply {
            directoryURL = folderURL
        }

        uiViewController.presentViewController(documentPicker, animated = true, completion = null)
    }

    override fun checkAndRequestRecordingPermission(): Boolean {
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
}

val AppContext.uiViewControllerContext: ViewControllerContext
    get() = this as ViewControllerContext
