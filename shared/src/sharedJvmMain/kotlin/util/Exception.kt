package util

import kotlin.system.exitProcess

actual fun setupUncaughtExceptionHandler(onException: (Throwable) -> Unit) {
    Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
        onException(throwable)
        if (isAndroid) {
            exitProcess(2)
        }
    }
}
