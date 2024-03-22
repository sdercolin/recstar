package model

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import exception.ReclistNoValidLineException
import exception.TextDecodeFailureException
import io.File
import io.readTextWithEncodingOrNull
import kotlinx.serialization.Serializable
import util.Encoding
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
    val comments: Map<String, String>?,
) : JavaSerializable {
    companion object {
        const val FILE_EXTENSION = "txt"
        const val FILE_NAME_SUFFIX = ".txt"
        const val COMMENT_FILE_NAME_SUFFIX_EXTENSION = "-comment.txt"
    }
}

fun parseReclist(
    file: File,
    fileEncoding: Encoding?,
    commentFile: File?,
    commentFileEncoding: Encoding?,
): Result<Reclist> =
    runCatching {
        val lines = runCatching { file.readTextWithEncodingOrNull(fileEncoding) }
            .getOrElse {
                if (file.exists() && file.isFile) {
                    throw TextDecodeFailureException()
                } else {
                    throw it
                }
            }
            .split(*lineSeparators).filter { it.isValidFileName() }
        if (lines.isEmpty()) throw ReclistNoValidLineException()
        val comments = commentFile?.readTextWithEncodingOrNull(commentFileEncoding)?.split(*commentLineSeparators)
            ?.filterNot { it.startsWith("#") }
            ?.mapNotNull { line ->
                val sections = line.split(*commentKeyValueSeparators, limit = 2)
                if (sections.size != 2) {
                    null
                } else {
                    if (sections[0].isBlank() || sections[1].isBlank()) {
                        null
                    } else {
                        sections[0] to sections[1]
                    }
                }
            }
            ?.toMap()
        Reclist(file.nameWithoutExtension, file.absolutePath, lines, comments)
    }

private val lineSeparators = arrayOf(' ', '\n', '\r').toCharArray()

/**
 * See OREMO docs for more info about the comment file format. http://nwp8861.blog92.fc2.com/blog-entry-335.html.
 */
private val commentLineSeparators = arrayOf('\n', '\r').toCharArray()
private val commentKeyValueSeparators = arrayOf(' ', '\t', ':').toCharArray()
