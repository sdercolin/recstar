package util

import ui.model.AppContext
import java.awt.Desktop
import java.net.URI

actual object Browser {
    actual fun openUrl(
        appContext: AppContext,
        url: String,
    ) {
        Desktop.getDesktop().browse(URI(url))
    }
}
