package exception

import ui.string.*

abstract class LocalizedException(
    private val stringKey: Strings,
    private val args: List<Any?>? = null,
    cause: Throwable? = null,
) :
    Exception(cause) {
    override val message: String?
        get() = stringStatic(stringKey, *args.orEmpty().toTypedArray())
}
