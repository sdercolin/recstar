package util

import io.File
import io.Paths
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier
import io.logsDirectory

object Log {
    private val napierDisabled get() = isDesktop
    private val logFile get() = Paths.logsDirectory.resolve("error.log")

    fun initialize() {
        if (Paths.logsDirectory.exists().not()) {
            val success = Paths.logsDirectory.mkdirs()
            if (!success) {
                println("Failed to create logs directory")
            }
        }
        if (!napierDisabled) {
            Napier.base(DebugAntilog())
            Napier.base(FileAntilog(logFile, LogLevel.ERROR))
        }
        setupUncaughtExceptionHandler { throwable ->
            e(throwable)
        }
    }

    fun d(message: String) {
        if (napierDisabled) {
            nativePerformLog(LogLevel.DEBUG, null, null, message)
        } else {
            Napier.d(message)
        }
    }

    fun i(message: String) {
        if (napierDisabled) {
            nativePerformLog(LogLevel.INFO, null, null, message)
        } else {
            Napier.i(message)
        }
    }

    fun w(message: String) {
        if (napierDisabled) {
            nativePerformLog(LogLevel.WARNING, null, null, message)
        } else {
            Napier.w(message)
        }
    }

    fun w(
        message: String,
        throwable: Throwable,
    ) {
        if (napierDisabled) {
            nativePerformLog(LogLevel.WARNING, null, throwable, message)
        } else {
            Napier.w(message, throwable)
        }
    }

    fun w(throwable: Throwable) {
        if (napierDisabled) {
            nativePerformLog(LogLevel.WARNING, null, throwable, null)
        } else {
            Napier.w(throwable.message ?: "", throwable)
        }
    }

    fun e(message: String) {
        if (napierDisabled) {
            nativePerformLog(LogLevel.ERROR, null, null, message)
        } else {
            Napier.e(message)
        }
    }

    fun e(
        message: String,
        throwable: Throwable,
    ) {
        if (napierDisabled) {
            nativePerformLog(LogLevel.ERROR, null, throwable, message)
        } else {
            Napier.e(message, throwable)
        }
    }

    fun e(throwable: Throwable) {
        if (napierDisabled) {
            nativePerformLog(LogLevel.ERROR, null, throwable, null)
        } else {
            Napier.e(throwable.message ?: "", throwable)
        }
    }
}

private class FileAntilog(val file: File, val minLevel: LogLevel) : Antilog() {
    override fun isEnable(
        priority: LogLevel,
        tag: String?,
    ): Boolean = priority >= minLevel

    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?,
    ) = writeLogToFile(file, priority, tag, throwable, message)
}

private fun nativePerformLog(
    priority: LogLevel,
    tag: String?,
    throwable: Throwable?,
    message: String?,
) {
    printLog(priority, tag, throwable, message)
    if (priority >= LogLevel.ERROR) {
        writeLogToFile(Paths.logsDirectory.resolve("error.log"), priority, tag, throwable, message)
    }
}

private fun printLog(
    priority: LogLevel,
    tag: String?,
    throwable: Throwable?,
    message: String?,
) {
    val log = buildLog(priority, tag, throwable, message)
    println(log)
}

private fun writeLogToFile(
    file: File,
    priority: LogLevel,
    tag: String?,
    throwable: Throwable?,
    message: String?,
) {
    val log = buildLog(priority, tag, throwable, message)
    file.appendText("$log\n")
}

private fun buildLog(
    priority: LogLevel,
    tag: String?,
    throwable: Throwable?,
    message: String?,
): String {
    val time = DateTime.getNowString()
    val tagText = tag?.let { "[$it] " } ?: ""
    val baseLogString = "$time $priority: $tagText${message.orEmpty()}"
    return if (throwable != null) {
        "$baseLogString\n${throwable.stackTraceToString()}"
    } else {
        baseLogString
    }
}
