package io

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.BooleanVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSMutableData
import platform.Foundation.NSString
import platform.Foundation.NSStringEncoding
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.appendData
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.lastPathComponent
import platform.Foundation.stringByAppendingPathComponent
import platform.Foundation.stringByDeletingLastPathComponent
import platform.Foundation.stringEncodingForData
import platform.Foundation.writeToFile
import util.Encoding
import util.toNSString
import util.toNSStringEncoding
import util.withNSError

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class File actual constructor(private val path: String) {
    private val fileManager: NSFileManager = NSFileManager.defaultManager

    actual val name: String
        get() = path.toNSString().lastPathComponent

    actual val nameWithoutExtension: String
        get() = name.substringBeforeLast('.', "")

    actual fun exists(): Boolean = fileManager.fileExistsAtPath(path)

    private fun isDir(): Boolean? =
        memScoped {
            val isDirectory = alloc<BooleanVar>()
            val exists = fileManager.fileExistsAtPath(path, isDirectory.ptr)
            if (exists) {
                isDirectory.value
            } else {
                null
            }
        }

    actual val absolutePath: String = path

    actual val isFile: Boolean
        get() = isDir() == false
    actual val isDirectory: Boolean
        get() = isDir() == true

    actual fun listFiles(): List<File> =
        withNSError { e ->
            val nsStringPath = path.toNSString()
            val contents = fileManager.contentsOfDirectoryAtPath(path, e)
            contents?.map {
                File(nsStringPath.stringByAppendingPathComponent(it as String))
            }.orEmpty()
        }

    actual val parentFile: File?
        get() = path.toNSString().stringByDeletingLastPathComponent().let {
            if (it.isEmpty()) {
                null
            } else {
                File(it)
            }
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

    actual fun readText(encoding: Encoding?): String {
        if (!fileManager.fileExistsAtPath(path)) {
            throw RuntimeException("File does not exist at path: $path")
        }

        val contents = fileManager.contentsAtPath(path)
        if (contents == null || contents.length.toInt() == 0) {
            throw RuntimeException("Cannot read file at path: $path")
        }

        val encodingToUse = encoding?.toNSStringEncoding() ?: detectEncoding(contents)

        val nsString = NSString.create(contents, encodingToUse)
            ?: throw RuntimeException("Failed to decode file content using encoding $encodingToUse at path: $path")

        return nsString.toString()
    }

    private fun detectEncoding(data: NSData): NSStringEncoding =
        memScoped {
            val string = alloc<ObjCObjectVar<String?>>()
            return NSString.stringEncodingForData(
                data = data,
                encodingOptions = null,
                convertedString = string.ptr,
                usedLossyConversion = null,
            ).takeUnless { it == 0.toULong() } ?: NSUTF8StringEncoding
        }

    actual fun appendText(text: String) {
        val fileManager = NSFileManager.defaultManager()
        if (fileManager.fileExistsAtPath(path)) {
            val url = NSURL.fileURLWithPath(path)
            val data = NSMutableData.dataWithContentsOfURL(url) as NSMutableData?
            requireNotNull(data)
            data.appendData(text.toNSString().dataUsingEncoding(NSUTF8StringEncoding)!!)
            data.writeToFile(path, true)
        } else {
            writeText(text)
        }
        Unit
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
