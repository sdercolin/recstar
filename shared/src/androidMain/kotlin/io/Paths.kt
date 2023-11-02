package io

import android.content.Context
import util.JavaFile
import java.lang.ref.WeakReference

actual object Paths {
    actual val appRoot: File get() = File(context.filesDir)
    actual val contentRoot: File get() = File(contentRootFile)
    val cacheRoot: File get() = File(context.cacheDir)

    private lateinit var contentRootFile: JavaFile
    private lateinit var contextRef: WeakReference<Context>

    private val context: Context
        get() = contextRef.get() ?: error("Context not initialized")

    fun initializeContext(context: Context) {
        this.contextRef = WeakReference(context)
        contentRootFile = context.getExternalFilesDir(null) ?: context.filesDir
    }
}
