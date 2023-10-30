package util

import org.apache.tika.parser.txt.CharsetDetector

fun ByteArray.detectEncoding(): String? {
    val detector = CharsetDetector()
    detector.setText(this)
    return detector.detect().name
}
