package util

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * A helper class for getting the current date and time.
 */
object DateTime {
    /**
     * Gets the current time in milliseconds.
     */
    fun getNow(): Long = Clock.System.now().toEpochMilliseconds()

    /**
     * Gets the current time as a string.
     */
    fun getNowString(): String = Clock.System.now().toString()

    /**
     * Gets the current time as a string that is readable by humans.
     *
     * @param localTime Whether to use the local time or UTC.
     */
    fun getNowReadableString(localTime: Boolean = true): String =
        Clock.System.now()
            .run {
                if (localTime) {
                    toLocalDateTime(TimeZone.currentSystemDefault())
                } else {
                    this
                }
            }
            .toString()
            .replace("T", " ")
            .replace("Z", "")
            .substringBeforeLast(".")
}
