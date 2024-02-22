package model

import ui.string.*

enum class SortingMethod(override val textKey: Strings) : LocalizedText {
    NameAsc(Strings.SortingMethodNameAsc),
    NameDesc(Strings.SortingMethodNameDesc),
    UsedAsc(Strings.SortingMethodUsedAsc),
    UsedDesc(Strings.SortingMethodUsedDesc),
    ;

    fun <T : ListItem<T>> sort(list: List<T>): List<T> =
        when (this) {
            NameAsc -> list.sortedBy { it.name }
            NameDesc -> list.sortedByDescending { it.name }
            UsedAsc -> list.sortedBy { it.lastUsed }
            UsedDesc -> list.sortedByDescending { it.lastUsed }
        }
}
