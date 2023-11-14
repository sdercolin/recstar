package util

import android.content.Intent
import android.net.Uri
import ui.model.AppContext
import ui.model.androidNativeContext

actual object Browser {
    actual fun openUrl(
        appContext: AppContext,
        url: String,
    ) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        appContext.androidNativeContext.startActivity(intent)
    }
}
