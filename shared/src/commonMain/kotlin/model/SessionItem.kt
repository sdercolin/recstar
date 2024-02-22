package model

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class SessionItem(
    override val name: String,
    val lastUsed: Long,
) : ListItem<SessionItem>, JavaSerializable {
    override val sortableName: String
        get() = name

    override val sortableUsedTime: Long
        get() = lastUsed
}
