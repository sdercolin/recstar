package ui.string

import ui.string.Strings.*

fun Strings.ja(): String? =
    when (this) {
        CommonOkay -> "OK"
        CommonCancel -> "キャンセル"
        CommonYes -> "はい"
        CommonNo -> "いいえ"
        CommonBack -> "戻る"
        else -> null
    }
