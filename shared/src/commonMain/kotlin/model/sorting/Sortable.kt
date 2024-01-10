package model.sorting

interface Sortable<T : Any> {
    val sortableName: String
    val sortableUsedTime: Long
}
