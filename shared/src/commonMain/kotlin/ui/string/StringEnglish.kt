package ui.string

import ui.string.Strings.*

fun Strings.en(): String =
    when (this) {
        CommonOkay -> "OK"
        CommonCancel -> "Cancel"
        CommonYes -> "Yes"
        CommonNo -> "No"
        CommonBack -> "Back"
    }
