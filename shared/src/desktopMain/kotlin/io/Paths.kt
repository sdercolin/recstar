package io

import const.APP_NAME
import util.JavaFile
import util.isMacOS
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

    actual val contentRoot: File
        get() {
            val javaFile = when {
                isMacOS -> home.resolve("Documents").resolve(APP_NAME)
                else -> home.resolve(APP_NAME)
            }
            return File(javaFile)
        }
}
