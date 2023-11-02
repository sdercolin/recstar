package io

import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import ui.common.AlertDialogController
import ui.common.ToastController
import ui.common.requestConfirm
import ui.model.AppContext
import ui.model.uiViewControllerContext
import ui.string.*
import util.Log

actual class FileInteractor actual constructor(
    context: AppContext,
    toastController: ToastController,
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
            alertDialogController.requestConfirm(message = stringStatic(Strings.ErrorReadFileFailedMessage))
        }
    }

    actual fun requestOpenFolder(folder: File) {
        val folderURL = folder.toNSURL()
        val documentPicker = UIDocumentPickerViewController(
            documentTypes = listOf("public.folder"),
            inMode = UIDocumentPickerMode.UIDocumentPickerModeOpen,
        ).apply {
            directoryURL = folderURL
        }

        uiViewController.presentViewController(documentPicker, animated = true, completion = null)
    }

    actual fun pickFile(
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
}
