package model

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import io.File
import kotlinx.serialization.Serializable
import util.isValidFileName

/**
 * Model for a reclist.
 *
 * @property name The name of the reclist.
 * @property path The absolute path to the reclist file.
 * @property lines The lines of the reclist file.
 */
@Immutable
@Serializable
data class Reclist(
    val name: String,
    val path: String,
    val lines: List<String>,
) : JavaSerializable

fun parseReclist(file: File): Result<Reclist> =
    runCatching {
        val lines = file.readTextDetectEncoding().split(*separators).filter { it.isValidFileName() }
        Reclist(file.nameWithoutExtension, file.absolutePath, lines)
    }

private val separators = arrayOf(' ', '\t', '\n', '\r', '　').toCharArray()
