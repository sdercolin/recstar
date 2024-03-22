package io

import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered
import util.Encoding
import util.JavaFile
import util.detectEncoding
import util.toFile
import java.nio.charset.Charset

actual class File actual constructor(path: String) {
    constructor(file: JavaFile) : this(file.absolutePath)

    private val internalFile = JavaFile(path)

    actual val name: String
        get() = internalFile.name

    actual val nameWithoutExtension: String
        get() = internalFile.nameWithoutExtension

    actual val extension: String
        get() = internalFile.extension

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

    actual fun delete(): Boolean = internalFile.deleteRecursively()

    actual var lastModified: Long
        get() = internalFile.lastModified()
        set(value) {
            internalFile.setLastModified(value)
        }

    actual fun readText(encoding: Encoding): String {
        return internalFile.readText(Charset.forName(encoding.value))
    }

    actual fun readTextDetectEncoding(): String {
        val bytes = internalFile.readBytes()
        val detectedEncoding = bytes.detectEncoding()
        return internalFile.readText(Charset.forName(detectedEncoding))
    }

    actual fun writeText(text: String) = internalFile.writeText(text)

    actual fun appendText(text: String) = internalFile.appendText(text)

    actual fun copyTo(
        target: File,
        overwrite: Boolean,
    ) {
        internalFile.copyRecursively(target.internalFile, overwrite)
    }

    actual fun resolve(path: String): File = File(internalFile.resolve(path))

    actual fun source(): Source = internalFile.inputStream().asSource().buffered()

    override fun equals(other: Any?): Boolean =
        internalFile.absolutePath == (other as? File)?.internalFile?.absolutePath
}

fun String.toFile(): File = File(this)
