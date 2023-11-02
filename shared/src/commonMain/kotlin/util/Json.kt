package util

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * A global [Json] instance for readable JSON contents.
 */
val json = Json {
    isLenient = true
    ignoreUnknownKeys = true
    prettyPrint = true
    encodeDefaults = true
}

/**
 * Parse a JSON string to an object.
 */
inline fun <reified T> String.parseJson(): T {
    return json.decodeFromString(this)
}

/**
 * Serialize an object to a JSON string.
 */
inline fun <reified T> T.stringifyJson(): String {
    return json.encodeToString(this)
}
