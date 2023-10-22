package util

typealias JavaFile = java.io.File

fun String.toJavaFile(): JavaFile = JavaFile(this)