package model.sorting

import ui.string.*

enum class SortingMethod(override val textKey: Strings) : LocalizedText {
    NameAsc(Strings.SortingMethodNameAsc),
    NameDesc(Strings.SortingMethodNameDesc),
    UsedAsc(Strings.SortingMethodUsedAsc),
    UsedDesc(Strings.SortingMethodUsedDesc),
    ;

    fun <T : Sortable<T>> sort(list: List<T>): List<T> =
        when (this) {
            NameAsc -> list.sortedBy { it.sortableName }
            NameDesc -> list.sortedByDescending { it.sortableName }
            UsedAsc -> list.sortedBy { it.sortableUsedTime }
            UsedDesc -> list.sortedByDescending { it.sortableUsedTime }
        }
}
