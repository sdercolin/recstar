package util

fun String.containsJapaneseVoiceMark(): Boolean {
    return characters.any { this.contains(it) }
}

private val characters = """
    がぎぐげござじずぜぞだぢづでどばびぶべぼぱぴぷぺぽゔゞガギグゲゴザジズゼゾダヂヅデドバビブベボパピプペポヷヸヹヺヴヾ
""".trimIndent() + 0x3099.toChar() + 0x309A.toChar()
