package io

import util.Log

expect object Paths {
    val appRoot: File
    val contentRoot: File
}

val Paths.logsDirectory: File
    get() = appRoot.resolve("logs")

val Paths.sessionsDirectory: File
    get() = contentRoot.resolve("sessions")

val Paths.reclistsDirectory: File
    get() = contentRoot.resolve("reclists")

fun ensurePaths() {
    listOf(Paths.appRoot, Paths.contentRoot).forEach {
        Log.i("ensurePaths: ${it.absolutePath}")
        if (!it.exists()) {
            Log.i("not exists, creating...")
            val created = it.mkdirs()
            Log.i("created: $created")
        }
    }
}
