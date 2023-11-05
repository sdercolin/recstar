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

fun ByteArray.toShortArray(
    target: ShortArray = ShortArray(size / 2),
    isLittleEndian: Boolean = false,
): ShortArray {
    for (i in target.indices) {
        if (isLittleEndian) {
            target[i] = (this[i * 2].toInt() and 0xff or (this[i * 2 + 1].toInt() shl 8)).toShort()
        } else {
            target[i] = (this[i * 2 + 1].toInt() and 0xff or (this[i * 2].toInt() shl 8)).toShort()
        }
    }
    return target
}
