package io

import kotlin.concurrent.AtomicReference

actual object Paths {
    private val appRootPathRef = AtomicReference<String?>(null)
    private val contentRootPathRef = AtomicReference<String?>(null)

    actual val appRoot: File
        get() {
            val path = appRootPathRef.value
                ?: throw IllegalStateException("appRootPath is not initialized")
            return File(path)
        }

    actual val contentRoot: File
        get() {
            val path = contentRootPathRef.value
                ?: throw IllegalStateException("contentRootPath is not initialized")
            return File(path)
        }

    fun initializeAppRootPath(path: String) {
        appRootPathRef.compareAndSet(null, path)
        println("appRootPath is set to $path")
    }

    fun initializeContentRootPath(path: String) {
        contentRootPathRef.compareAndSet(null, path)
        println("contentRootPath is set to $path")
    }
}
