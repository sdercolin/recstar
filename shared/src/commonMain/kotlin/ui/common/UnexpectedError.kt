package ui.common

import exception.FatalException
import exception.LocalizedException
import io.FileInteractor
import io.Paths
import io.logsDirectory
import ui.model.AppContext
import ui.string.*
import util.Clipboard
import util.Log
import util.isDesktop

class ErrorNotifier(
    private val alertDialogController: AlertDialogController,
    private val context: AppContext,
    private val fileInteractor: FileInteractor,
    private val onFatalError: () -> Unit,
) {
    fun notify(t: Throwable) {
        Log.e(t)
        if (t is LocalizedException) {
            alertDialogController.requestConfirmError(
                message = t.message ?: t.toString(),
                onFinish = { if (t is FatalException) onFatalError() },
            )
            return
        }
        if (isDesktop) {
            alertDialogController.requestConfirmCancellable(
                message = stringStatic(Strings.AlertUnexpectedErrorOpenLog),
                confirmButton = stringStatic(Strings.AlertUnexpectedErrorOpenLogButton),
                onConfirm = {
                    fileInteractor.requestOpenFolder(Paths.logsDirectory)
                },
            )
        } else {
            alertDialogController.requestConfirmCancellable(
                message = stringStatic(Strings.AlertUnexpectedErrorCopyLogToClipboard),
                confirmButton = stringStatic(Strings.AlertUnexpectedErrorCopyLogToClipboardButton),
                onConfirm = {
                    Clipboard.copy(appContext = context, text = t.stackTraceToString())
                },
            )
        }
    }
}
