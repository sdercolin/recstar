package io

import const.APP_NAME
import util.isMacOS
import util.toJavaFile

actual object Paths {
    private val home: String get() = System.getProperty("user.home")

    actual val appRoot: File
        get() {
            val home = home.toJavaFile()
            val javaFile = when {
                isMacOS -> home.resolve("Library").resolve(APP_NAME)
                else -> home.resolve(".$APP_NAME")
            }
            return File(javaFile)
        }

    actual val contentRoot: File
        get() {
            val home = home.toJavaFile()
            val javaFile = when {
                isMacOS -> home.resolve("Documents").resolve(APP_NAME)
                else -> home.resolve(APP_NAME)
            }
            return File(javaFile)
        }
}
