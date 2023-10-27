package util

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.free
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSError
import kotlin.concurrent.AtomicReference
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.concurrent.freeze

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
inline fun <T> withNSError(block: (CPointer<ObjCObjectVar<NSError?>>) -> T): T {
    memScoped {
        val error = nativeHeap.alloc<ObjCObjectVar<NSError?>>()
        val result = block(error.ptr)
        val nsError = error.value
        if (nsError != null) {
            throw RuntimeException(nsError.localizedDescription).also {
                nativeHeap.free(error)
            }
        }
        nativeHeap.free(error)
        return result
    }
}

@OptIn(ExperimentalForeignApi::class)
inline fun <T> withNSErrorCatching(block: (CPointer<ObjCObjectVar<NSError?>>) -> T) =
    runCatching {
        withNSError(block)
    }

@OptIn(FreezingIsDeprecated::class, ExperimentalNativeApi::class)
actual fun setupUncaughtExceptionHandler(onException: (Throwable) -> Unit) {
    val prevHook = AtomicReference<ReportUnhandledExceptionHook?>(null)
    val wrappedHook: ReportUnhandledExceptionHook = {
        onException(it)
        prevHook.value?.invoke(it)
        terminateWithUnhandledException(it)
    }
    prevHook.value = setUnhandledExceptionHook(wrappedHook.freeze())
}
