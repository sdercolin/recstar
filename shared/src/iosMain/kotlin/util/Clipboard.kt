package util

import platform.UIKit.UIPasteboard
import ui.model.AppContext

actual object Clipboard {
    actual fun copy(
        appContext: AppContext,
        text: String,
    ) {
        UIPasteboard.generalPasteboard().string = text
    }
}
