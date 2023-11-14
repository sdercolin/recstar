package util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import ui.model.AppContext
import ui.model.androidNativeContext

actual object Clipboard {
    actual fun copy(
        appContext: AppContext,
        text: String,
    ) {
        val context = appContext.androidNativeContext
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)
    }
}
