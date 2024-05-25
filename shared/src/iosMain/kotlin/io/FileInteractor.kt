package io

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSDirectoryEnumerationSkipsHiddenFiles
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSURL.Companion.URLWithString
import platform.Foundation.pathExtension
import platform.UIKit.UIActivityTypeAddToReadingList
import platform.UIKit.UIActivityTypeAssignToContact
import platform.UIKit.UIActivityTypeSaveToCameraRoll
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIPopoverArrowDirectionAny
import platform.UIKit.UIUserInterfaceIdiomPad
import platform.UIKit.popoverPresentationController
import ui.common.AlertDialogController
import ui.common.ToastController
import ui.common.requestConfirm
import ui.common.requestConfirmError
import ui.common.show
import ui.model.AppContext
import ui.model.uiViewControllerContext
import ui.string.*
import util.Log
import util.withNSError

@OptIn(ExperimentalForeignApi::class)
actual class FileInteractor actual constructor(
    private val context: AppContext,
    private val toastController: ToastController,
    private val alertDialogController: AlertDialogController,
) {
    private val uiViewController = context.uiViewControllerContext.uiViewController
    private var documentPickerDelegateCallback: ((File?) -> Unit)? = null

    // We need to keep a reference to the delegate, otherwise it will be garbage collected and the picker won't work.
    private val documentPickerDelegate = DocumentPickerDelegate { url ->
        url ?: return@DocumentPickerDelegate documentPickerDelegateCallback?.invoke(null) ?: Unit
        try {
            documentPickerDelegateCallback?.invoke(url.path?.let { path -> File(path) })
            documentPickerDelegateCallback = null
        } catch (t: Throwable) {
            Log.e("Failed to load file", t)
            alertDialogController.requestConfirmError(message = stringStatic(Strings.ErrorReadFileFailedMessage))
        }
    }

    actual fun requestOpenFolder(folder: File) {
        val sharedUrl = folder.toNSURL().absoluteString?.replace(
            "file://",
            "shareddocuments://",
        ) ?: return
        val fileUrl = URLWithString(sharedUrl) ?: return
        val application = UIApplication.sharedApplication
        if (application.canOpenURL(fileUrl)) {
            application.openURL(fileUrl)
        }
    }

    actual fun pickFile(
        title: String,
        allowedExtensions: List<String>,
        onFinish: (File?) -> Unit,
        initialDirectory: File?,
    ) {
        documentPickerDelegateCallback = onFinish
        val types = Uti.mapExtensions(allowedExtensions)
        val documentPicker = UIDocumentPickerViewController(
            forOpeningContentTypes = types,
            asCopy = true,
        )
        documentPicker.delegate = documentPickerDelegate
        context.coroutineScope.launch(Dispatchers.Main) {
            uiViewController.presentModalViewController(documentPicker, animated = true)
        }
    }

    actual fun exportData(request: ExportDataRequest) {
        Log.i("Exporting data: ${request.folder.absolutePath}")
        request.onStart?.invoke()

        fun onError(t: Throwable) {
            Log.e("Failed to export data", t)
            request.onError?.invoke(t)
            alertDialogController.requestConfirmError(message = stringStatic(Strings.ErrorExportDataFailedMessage))
        }

        val fileManager = NSFileManager.defaultManager
        val directoryURL = request.folder.toNSURL()
        val allowedExtensionsSet = request.allowedExtension.toSet()

        val fileURLs = try {
            withNSError { e ->
                val contents = fileManager.contentsOfDirectoryAtURL(
                    directoryURL,
                    null,
                    NSDirectoryEnumerationSkipsHiddenFiles,
                    e,
                )
                contents.orEmpty().filterIsInstance<NSURL>().filter { url ->
                    request.allowedExtension.isEmpty() || allowedExtensionsSet.contains(url.pathExtension)
                }
            }
        } catch (t: Throwable) {
            onError(RuntimeException("Failed to list files", t))
            return
        }

        if (fileURLs.isEmpty()) {
            alertDialogController.requestConfirm(message = stringStatic(Strings.AlertExportNoData))
            request.onCancel?.invoke()
            return
        }

        UIActivityViewController(fileURLs, null).apply {
            excludedActivityTypes = listOf(
                UIActivityTypeAddToReadingList,
                UIActivityTypeAssignToContact,
                UIActivityTypeSaveToCameraRoll,
            )

            // Set up a completion handler if you want to do something when the share action completes
            completionWithItemsHandler =
                { _: String?, completed: Boolean, _: List<*>?, error: NSError? ->
                    when {
                        completed -> {
                            Log.i("Exported data: ${request.folder.absolutePath}")
                            request.onSuccess?.invoke()
                            toastController.show(stringStatic(Strings.ToastExportDataSuccess))
                        }
                        error != null -> {
                            Log.e("Failed to export data: ${error.localizedDescription}")
                            request.onError?.invoke(
                                RuntimeException("Failed to export data: ${error.localizedDescription}"),
                            )
                            alertDialogController.requestConfirmError(
                                message = stringStatic(Strings.ErrorExportDataFailedMessage),
                            )
                        }
                        else -> {
                            Log.i("Cancelled exporting data: ${request.folder.absolutePath}")
                            request.onCancel?.invoke()
                            toastController.show(stringStatic(Strings.ToastExportDataCancel))
                        }
                    }
                }

            context.coroutineScope.launch(Dispatchers.Main) {
                if (UIDevice.currentDevice.userInterfaceIdiom == UIUserInterfaceIdiomPad) {
                    popoverPresentationController?.sourceView = uiViewController.view
                    popoverPresentationController?.sourceRect = CGRectMake(
                        uiViewController.view.bounds.useContents { this.origin.x + this.size.width / 2 },
                        uiViewController.view.bounds.useContents { this.origin.y + this.size.height / 2 },
                        0.0,
                        0.0,
                    )
                    popoverPresentationController?.permittedArrowDirections = UIPopoverArrowDirectionAny
                }

                uiViewController.presentModalViewController(this@apply, true)
            }
        }
    }
}
