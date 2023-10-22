package io

actual object Paths {

    actual val appRoot: File
        get() = File(appRootPath)

    private lateinit var appRootPath: String

    fun initializeAppRootPath(path: String) {
        appRootPath = path
    }
}

