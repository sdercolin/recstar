package model

import model.sorting.Sortable

interface ListItem<T : Any> : Sortable<T> {
    val name: String
}
