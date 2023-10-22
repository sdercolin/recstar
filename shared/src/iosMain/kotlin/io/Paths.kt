package io

import kotlin.concurrent.AtomicReference

actual object Paths {

    private val appRootPathRef = AtomicReference<String?>(null)

    actual val appRoot: File
        get() {
            val path = appRootPathRef.value
                ?: throw IllegalStateException("appRootPath is not initialized")
            return File(path)
        }

    fun initializeAppRootPath(path: String) {
        val isSet = appRootPathRef.compareAndSet(null, path)
        if (!isSet) {
            // handle or log the case where it was already set, if needed
        }
    }
}