package model.sorting

interface SortableListOwner<T : Sortable<T>> {
    val allowedSortingMethods: List<SortingMethod>
    var sortingMethod: SortingMethod
    var sortableList: List<T>

    fun sort() {
        val list = sortableList.toMutableList()
        when (sortingMethod) {
            SortingMethod.NameAsc -> list.sortBy { it.sortableName }
            SortingMethod.NameDesc -> list.sortByDescending { it.sortableName }
            SortingMethod.UsedAsc -> list.sortBy { it.sortableUsedTime }
            SortingMethod.UsedDesc -> list.sortByDescending { it.sortableUsedTime }
        }
        sortableList = list
    }
}
