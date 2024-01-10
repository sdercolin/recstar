package model.sorting

import ui.string.*

enum class SortingMethod(override val textKey: Strings) : LocalizedText {
    NameAsc(Strings.SortingMethodNameAsc),
    NameDesc(Strings.SortingMethodNameDesc),
    UsedAsc(Strings.SortingMethodUsedAsc),
    UsedDesc(Strings.SortingMethodUsedDesc),
}
