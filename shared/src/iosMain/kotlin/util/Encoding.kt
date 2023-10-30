package util

import platform.Foundation.NSASCIIStringEncoding
import platform.Foundation.NSISOLatin1StringEncoding
import platform.Foundation.NSJapaneseEUCStringEncoding
import platform.Foundation.NSNEXTSTEPStringEncoding
import platform.Foundation.NSShiftJISStringEncoding
import platform.Foundation.NSStringEncoding
import platform.Foundation.NSUTF8StringEncoding

fun Encoding.toNSStringEncoding(): NSStringEncoding =
    when (this) {
        Encoding.UTF8 -> NSUTF8StringEncoding
        Encoding.ShiftJIS -> NSShiftJISStringEncoding
        Encoding.EUCJP -> NSJapaneseEUCStringEncoding
        Encoding.GBK -> NSNEXTSTEPStringEncoding
        Encoding.GB2312 -> NSASCIIStringEncoding
        Encoding.EUCKR -> NSISOLatin1StringEncoding
    }
