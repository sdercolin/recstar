package util

import kotlinx.coroutines.CancellationException

/**
 * A [runCatching] that does not catch [CancellationException].
 */
inline fun <T, R> T.runCatchingCancellable(block: T.() -> R): Result<R> =
    try {
        Result.success(block())
    } catch (e: Throwable) {
        if (e is CancellationException) {
            throw e
        }
        Result.failure(e)
    }
