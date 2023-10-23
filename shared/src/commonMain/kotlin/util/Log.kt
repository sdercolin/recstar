package util

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

object Log {

    private var systemOutEnabled = false

    fun initialize(enableSystemOut: Boolean = false) {
        println("Log.init called")
        Napier.base(DebugAntilog())
        systemOutEnabled = enableSystemOut
    }

    fun d(message: String) {
        if (systemOutEnabled) {
            println("DEBUG: $message")
        } else {
            Napier.d(message)
        }
    }

    fun i(message: String) {
        if (systemOutEnabled) {
            println("INFO: $message")
        } else {
            Napier.i(message)
        }
    }

    fun w(message: String) {
        if (systemOutEnabled) {
            println("WARN: $message")
        } else {
            Napier.w(message)
        }
    }

    fun w(message: String, throwable: Throwable) {
        if (systemOutEnabled) {
            println("WARN: $message, throwable: $throwable")
        } else {
            Napier.w(message, throwable)
        }
    }

    fun w(throwable: Throwable) {
        if (systemOutEnabled) {
            println("WARN: throwable: $throwable")
        } else {
            Napier.w(throwable.message ?: "", throwable)
        }
    }

    fun e(message: String) {
        if (systemOutEnabled) {
            println("ERROR: $message")
        } else {
            Napier.e(message)
        }
    }

    fun e(message: String, throwable: Throwable) {
        if (systemOutEnabled) {
            println("ERROR: $message, throwable: $throwable")
        } else {
            Napier.e(message, throwable)
        }
    }

    fun e(throwable: Throwable) {
        if (systemOutEnabled) {
            println("ERROR: throwable: $throwable")
        } else {
            Napier.e(throwable.message ?: "", throwable)
        }
    }
}