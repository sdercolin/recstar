package io

import const.APP_NAME
import util.JavaFile
import util.Log
import util.isMacOS
import util.toFile
import util.toJavaFile

actual object Paths {
    val home: JavaFile get() = System.getProperty("user.home").toJavaFile()

    actual val appRoot: File
        get() {
            val javaFile = when {
                isMacOS -> home.resolve("Library").resolve(APP_NAME)
                else -> home.resolve(".$APP_NAME")
            }
            return File(javaFile)
        }

    var customContentRootLocation: JavaFile? = null

    actual val contentRoot: File
        get() {
            val location = customContentRootLocation ?: when {
                isMacOS -> home.resolve("Documents")
                else -> home
            }
            return location.resolve(APP_NAME).toFile()
        }

    actual fun moveContentRoot(newLocation: File) {
        val current = contentRoot
        val new = newLocation.resolve(APP_NAME)
        if (current == new) return
        customContentRootLocation = newLocation.toJavaFile()
        if (current.isDirectory.not()) return
        Log.i("Moving content root: $current -> $new")
        ensurePath(newLocation)
        current.copyTo(new, true)
        current.delete()
    }
}
