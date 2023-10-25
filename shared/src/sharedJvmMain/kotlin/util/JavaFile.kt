package util

import io.File

typealias JavaFile = java.io.File

fun String.toJavaFile(): JavaFile = JavaFile(this)

fun File.toJavaFile(): JavaFile = JavaFile(this.absolutePath)

fun JavaFile.toFile(): File = File(this)
