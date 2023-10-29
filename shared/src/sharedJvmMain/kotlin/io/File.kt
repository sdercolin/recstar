package io

import util.JavaFile
import util.toFile

actual class File actual constructor(path: String) {
    constructor(file: JavaFile) : this(file.absolutePath)

    private val internalFile = JavaFile(path)

    actual fun exists(): Boolean = internalFile.exists()

    actual val absolutePath: String
        get() = internalFile.absolutePath

    actual val isFile: Boolean
        get() = internalFile.isFile

    actual val isDirectory: Boolean
        get() = internalFile.isDirectory

    actual fun listFiles(): List<File> = internalFile.listFiles()?.map { it.toFile() }.orEmpty()

    actual val parentFile: File?
        get() = internalFile.parentFile?.toFile()

    actual fun mkdirs(): Boolean = internalFile.mkdirs()

    actual fun delete(): Boolean = internalFile.delete()

    actual fun writeText(text: String) = internalFile.writeText(text)

    actual fun readText(): String = internalFile.readText()

    actual fun appendText(text: String) = internalFile.appendText(text)

    actual fun resolve(path: String): File = File(internalFile.resolve(path))
}

fun String.toFile(): File = File(this)
