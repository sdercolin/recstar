package util

import ui.model.AppContext

expect object Browser {
    fun openUrl(
        appContext: AppContext,
        url: String,
    )
}
