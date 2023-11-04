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
     * @param withTime Whether to include the time. If false, [withSecond] and [withMilliSecond] are ignored.
     * @param withSecond Whether to include the seconds. If false, [withMilliSecond] is ignored.
     * @param withMilliSecond Whether to include milliseconds.
     */
    fun getNowReadableString(
        localTime: Boolean = true,
        withTime: Boolean = true,
        withSecond: Boolean = true,
        withMilliSecond: Boolean = false,
    ): String =
        Clock.System.now()
            .run {
                if (localTime) {
                    toLocalDateTime(TimeZone.currentSystemDefault()).toString()
                } else {
                    toString()
                }
            }
            .replace("Z", "")
            .runIf(!withTime) {
                substringBefore("T")
            }
            .runIf(!withSecond) {
                substringBeforeLast(":")
            }
            .runIf(!withMilliSecond) {
                substringBeforeLast(".")
            }
            .replace("T", " ")
}
