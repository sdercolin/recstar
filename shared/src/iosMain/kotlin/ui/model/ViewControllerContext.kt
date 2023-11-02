package ui.model

import io.DocumentPickerDelegate
import io.File
import io.Uti
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
import repository.ReclistRepository
import ui.common.AlertDialogController
import ui.common.ToastController
import ui.common.requestConfirm
import ui.string.*
import util.Log

class ViewControllerContext(
    val uiViewController: UIViewController,
    override val coroutineScope: CoroutineScope,
) : AppContext {
    override val reclistRepository: ReclistRepository = ReclistRepository(this)

    override val toastController: ToastController = ToastController(this)

    override val alertDialogController: AlertDialogController = AlertDialogController(this)

    private var documentPickerDelegateCallback: ((File?) -> Unit)? = null

    // We need to keep a reference to the delegate, otherwise it will be garbage collected and the picker won't work.
    private val documentPickerDelegate = DocumentPickerDelegate { url ->
        url ?: return@DocumentPickerDelegate documentPickerDelegateCallback?.invoke(null) ?: Unit
        try {
            documentPickerDelegateCallback?.invoke(url.path?.let { path -> File(path) })
            documentPickerDelegateCallback = null
        } catch (t: Throwable) {
            Log.e("Failed to load file", t)
            alertDialogController.requestConfirm(message = stringStatic(Strings.ErrorReadFileFailedMessage))
        }
    }

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

    override fun pickFile(
        title: String,
        allowedExtensions: List<String>,
        onFinish: (File?) -> Unit,
    ) {
        documentPickerDelegateCallback = onFinish
        val types = Uti.mapExtensions(allowedExtensions)
        val documentPicker = UIDocumentPickerViewController(
            forOpeningContentTypes = types,
            asCopy = true,
        )
        documentPicker.delegate = documentPickerDelegate
        uiViewController.presentModalViewController(documentPicker, animated = true)
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
