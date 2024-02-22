package io

import util.Log

expect object Paths {
    val appRoot: File
    val contentRoot: File

    fun moveContentRoot(newLocation: File)
}

val Paths.logsDirectory: File
    get() = appRoot.resolve("logs")

val Paths.sessionsDirectory: File
    get() = contentRoot.resolve("sessions")

val Paths.reclistsDirectory: File
    get() = contentRoot.resolve("reclists")

val Paths.guideAudioDirectory: File
    get() = contentRoot.resolve("bgms")

val Paths.appRecordFile: File
    get() = appRoot.resolve("record.json")

val Paths.appPreferenceFile: File
    get() = appRoot.resolve("preference.json")

val Paths.sessionUsageRecordFile: File
    get() = appRoot.resolve("session-usage-record.json")
val Paths.guideAudioRecordFile: File
    get() = appRoot.resolve("guide-audio-usage-record.json")

val Paths.reclistRecordFile: File
    get() = appRoot.resolve("reclist-usage-record.json")

fun ensurePaths() {
    ensurePath(Paths.appRoot)
    ensurePath(Paths.contentRoot)
}

fun ensurePath(path: File) {
    Log.i("ensurePath: ${path.absolutePath}")
    if (!path.exists()) {
        Log.i("not exists, creating...")
        val created = path.mkdirs()
        Log.i("created: $created")
    }
}

private val splitters = charArrayOf('\\', '/')

val String.pathSections get() = trim(*splitters).split(*splitters)
val String.lastPathSection get() = pathSections.last()
