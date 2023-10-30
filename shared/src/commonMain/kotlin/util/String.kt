package util

private val invalidCharsForFileName = arrayOf('"', '*', ':', '<', '>', '?', '\\', '/', '|', Char(0x7F), '\u0000')

fun String.isValidFileName(): Boolean {
    return invalidCharsForFileName.none { contains(it) } && isNotBlank()
}
