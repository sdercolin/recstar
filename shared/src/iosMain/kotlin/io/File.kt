package io

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.BooleanVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileModificationDate
import platform.Foundation.NSInputStream
import platform.Foundation.NSMutableData
import platform.Foundation.NSString
import platform.Foundation.NSStringEncoding
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.appendData
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.lastPathComponent
import platform.Foundation.stringByAppendingPathComponent
import platform.Foundation.stringByDeletingLastPathComponent
import platform.Foundation.stringEncodingForData
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile
import util.Encoding
import util.Log
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

    actual val extension: String
        get() = name.substringAfterLast('.', "")

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

    actual var lastModified: Long
        get() = withNSError { e ->
            val attributes = fileManager.attributesOfItemAtPath(path, e) ?: return@withNSError 0L
            val date = attributes[NSFileModificationDate] as NSDate
            date.timeIntervalSince1970.toLong()
        }
        set(value) {
            withNSError { e ->
                val attributes = fileManager.attributesOfItemAtPath(path, e)?.toMutableMap() ?: mutableMapOf()
                val date = NSDate.dateWithTimeIntervalSince1970(value.toDouble())
                attributes[NSFileModificationDate] = date
                fileManager.setAttributes(attributes, ofItemAtPath = path, error = e)
            }
        }

    actual fun writeText(text: String) =
        withNSError { e ->
            text.toNSString().writeToFile(path, true, NSUTF8StringEncoding, e)
            Unit
        }

    actual fun readText(encoding: Encoding): String =
        readTextWith {
            encoding.toNSStringEncoding()
        }

    actual fun readTextDetectEncoding(): String =
        readTextWith {
            detectEncoding(it)
        }

    private fun readTextWith(encoding: (NSData) -> NSStringEncoding): String {
        if (!fileManager.fileExistsAtPath(path)) {
            throw RuntimeException("File does not exist at path: $path")
        }

        val contents = fileManager.contentsAtPath(path)
        if (contents == null || contents.length.toInt() == 0) {
            throw RuntimeException("Cannot read file at path: $path")
        }

        val encodingToUse = encoding(contents)

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

    actual fun copyTo(
        target: File,
        overwrite: Boolean,
    ) {
        Log.d("File.copyTo: $path -> ${target.absolutePath}")
        if (exists().not()) {
            throw RuntimeException("File does not exist at path: $path")
        }
        if (target.exists()) {
            if (!overwrite) {
                throw RuntimeException("File already exists at path: ${target.absolutePath}")
            }
            target.delete()
        }
        withNSError { e ->
            fileManager.copyItemAtPath(path, target.absolutePath, e)
        }
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

    actual fun source(): Source = NSInputStream(toNSURL()).asSource().buffered()

    fun toNSURL() = NSURL.fileURLWithPath(this.absolutePath)

    override fun equals(other: Any?): Boolean = absolutePath == (other as? File)?.absolutePath

    override fun hashCode(): Int = absolutePath.hashCode()
}

fun NSURL.toFile(): File = File(requireNotNull(this.path))
