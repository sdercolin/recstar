package util

import io.File

typealias JavaFile = java.io.File

fun String.toJavaFile(): JavaFile = JavaFile(this)
fun JavaFile.toFile(): File = File(this)