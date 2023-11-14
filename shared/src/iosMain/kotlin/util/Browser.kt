package util

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import ui.model.AppContext

actual object Browser {
    actual fun openUrl(
        appContext: AppContext,
        url: String,
    ) {
        UIApplication.sharedApplication.openURL(NSURL(string = url))
    }
}
