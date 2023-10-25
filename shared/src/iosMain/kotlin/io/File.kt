package io

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.BooleanVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.stringByAppendingPathComponent
import platform.Foundation.writeToFile
import util.toNSString
import util.withNSError

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class File actual constructor(private val path: String) {
    private val fileManager: NSFileManager = NSFileManager.defaultManager

    actual fun exists(): Boolean = fileManager.fileExistsAtPath(path)

    private fun isDir(): BooleanVar? =
        memScoped {
            val isDirectory = alloc<BooleanVar>()
            val exists = fileManager.fileExistsAtPath(path, isDirectory.ptr)
            if (exists) {
                isDirectory
            } else {
                null
            }
        }

    actual val absolutePath: String = path

    actual val isFile: Boolean
        get() = isDir()?.value == false
    actual val isDirectory: Boolean
        get() = isDir()?.value == true

    actual fun listFiles(): List<File> =
        withNSError { e ->
            val nsStringPath = path.toNSString()
            val contents = fileManager.contentsOfDirectoryAtPath(path, e)
            contents?.map {
                File(nsStringPath.stringByAppendingPathComponent(it as String))
            }.orEmpty()
        }

    actual fun mkdirs(): Boolean =
        withNSError { e ->
            return fileManager.createDirectoryAtPath(path, true, null, e)
        }

    actual fun delete(): Boolean =
        withNSError { e ->
            return fileManager.removeItemAtPath(path, e)
        }

    actual fun writeText(text: String) =
        withNSError { e ->
            text.toNSString().writeToFile(path, true, NSUTF8StringEncoding, e)
            Unit
        }

    actual fun readText(): String {
        if (!fileManager.fileExistsAtPath(path)) {
            throw RuntimeException("File does not exist at path: $path")
        }

        val contents = fileManager.contentsAtPath(path)
        if (contents == null || contents.length.toInt() == 0) {
            throw RuntimeException("Cannot read file at path: $path")
        }

        val nsString = NSString.create(contents, NSUTF8StringEncoding)
            ?: throw RuntimeException("Failed to decode file content from UTF-8 at path: $path")

        return nsString.toString()
    }

    actual fun resolve(path: String): File {
        val myPath = this.path.toNSString()
        return if (path.startsWith("/")) {
            File(path)
        } else {
            val resolvedPath = myPath.stringByAppendingPathComponent(path)
            File(resolvedPath)
        }
    }

    fun toNSURL() = NSURL.fileURLWithPath(this.absolutePath)
}
