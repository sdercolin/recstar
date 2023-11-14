package util

import ui.model.AppContext
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual object Clipboard {
    actual fun copy(
        appContext: AppContext,
        text: String,
    ) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val selection = StringSelection(text)
        clipboard.setContents(selection, selection)
    }
}
