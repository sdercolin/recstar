package util

import kotlinx.io.Buffer

fun Buffer.writeChar(value: Char) {
    writeByte(value.code.toByte())
}

fun Buffer.writeRawString(value: String) {
    for (i in value.indices) {
        writeChar(value[i])
    }
}

fun ByteArray.writeInt(
    offset: Int,
    value: Int,
) {
    this[offset] = (value and 0xff).toByte()
    this[offset + 1] = (value shr 8 and 0xff).toByte()
    this[offset + 2] = (value shr 16 and 0xff).toByte()
    this[offset + 3] = (value shr 24 and 0xff).toByte()
}
