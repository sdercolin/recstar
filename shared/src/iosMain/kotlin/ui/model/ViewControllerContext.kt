package ui.model

import io.File
import kotlinx.coroutines.CoroutineScope
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
import ui.common.AlertDialogController
import ui.common.ToastController

class ViewControllerContext(
    val uiViewController: UIViewController,
    override val coroutineScope: CoroutineScope,
) : AppContext {
    override val toastController: ToastController = ToastController(this)

    override val alertDialogController: AlertDialogController = AlertDialogController(this)

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

    override fun checkRecordingPermissionIgnored(): Boolean {
        val authStatus = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeAudio)
        return authStatus == AVAuthorizationStatusDenied || authStatus == AVAuthorizationStatusRestricted
    }
}

val AppContext.uiViewControllerContext: ViewControllerContext
    get() = this as ViewControllerContext
