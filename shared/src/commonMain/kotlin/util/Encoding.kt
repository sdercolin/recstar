package util

import kotlinx.serialization.Serializable

@Serializable
enum class Encoding(val value: String) {
    UTF8("UTF-8"),
    ShiftJIS("Shift-JIS"),
    EUCJP("EUC-JP"),
    GBK("GBK"),
    GB2312("GB2312"),
    EUCKR("EUC-KR"),
}
