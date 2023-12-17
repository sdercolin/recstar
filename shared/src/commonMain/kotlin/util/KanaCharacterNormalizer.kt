package util

/**
 * A utility class for normalizing Kana characters to NFC.
 */
object KanaCharacterNormalizer {
    fun convert(input: String): String {
        val builder = StringBuilder()
        var i = 0
        while (i < input.length) {
            val c = input[i]
            if (c == VOICED_MARK) {
                val prev = builder.lastOrNull()
                if (prev != null) {
                    val voiced = VOICED_MAP[prev.toString()]
                    if (voiced != null) {
                        builder.setLength(builder.length - 1)
                        builder.append(voiced)
                    }
                }
            } else if (c == SEMI_VOICED_MARK) {
                val prev = builder.lastOrNull()
                if (prev != null) {
                    val semiVoiced = SEMI_VOICED_MAP[prev.toString()]
                    if (semiVoiced != null) {
                        builder.setLength(builder.length - 1)
                        builder.append(semiVoiced)
                    }
                }
            } else {
                builder.append(c)
            }
            i++
        }
        return builder.toString()
    }
}

private const val VOICED_MARK = '\u3099'
private val VOICED_MAP = mapOf(
    "う" to "ゔ",
    "か" to "が",
    "き" to "ぎ",
    "く" to "ぐ",
    "け" to "げ",
    "こ" to "ご",
    "さ" to "ざ",
    "し" to "じ",
    "す" to "ず",
    "せ" to "ぜ",
    "そ" to "ぞ",
    "た" to "だ",
    "ち" to "ぢ",
    "つ" to "づ",
    "て" to "で",
    "と" to "ど",
    "は" to "ば",
    "ひ" to "び",
    "ふ" to "ぶ",
    "へ" to "べ",
    "ほ" to "ぼ",
    "ウ" to "ヴ",
    "カ" to "ガ",
    "キ" to "ギ",
    "ク" to "グ",
    "ケ" to "ゲ",
    "コ" to "ゴ",
    "サ" to "ザ",
    "シ" to "ジ",
    "ス" to "ズ",
    "セ" to "ゼ",
    "ソ" to "ゾ",
    "タ" to "ダ",
    "チ" to "ヂ",
    "ツ" to "ヅ",
    "テ" to "デ",
    "ト" to "ド",
    "ハ" to "バ",
    "ヒ" to "ビ",
    "フ" to "ブ",
    "ヘ" to "ベ",
    "ホ" to "ボ",
    "ワ" to "ヷ",
    "ヰ" to "ヸ",
    "ヱ" to "ヹ",
    "ヲ" to "ヺ",
    "ヽ" to "ヾ",
)

private const val SEMI_VOICED_MARK = '\u309A'
private val SEMI_VOICED_MAP = mapOf(
    "は" to "ぱ",
    "ひ" to "ぴ",
    "ふ" to "ぷ",
    "へ" to "ぺ",
    "ほ" to "ぽ",
    "ハ" to "パ",
    "ヒ" to "ピ",
    "フ" to "プ",
    "ヘ" to "ペ",
    "ホ" to "ポ",
)
