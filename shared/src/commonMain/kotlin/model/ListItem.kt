package model

interface ListItem<T : Any> {
    val name: String
    val lastUsed: Long

    fun usedTimeUpdated(usedTime: Long): T
}
