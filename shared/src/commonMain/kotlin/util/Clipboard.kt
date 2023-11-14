package util

import ui.model.AppContext

expect object Clipboard {
    fun copy(
        appContext: AppContext,
        text: String,
    )
}
