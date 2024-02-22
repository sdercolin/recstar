package model

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
class ReclistItem(
    override val name: String,
    override val lastUsed: Long,
) : ListItem<ReclistItem>, JavaSerializable {
    override fun usedTimeUpdated(usedTime: Long): ReclistItem = ReclistItem(name, usedTime)
}
