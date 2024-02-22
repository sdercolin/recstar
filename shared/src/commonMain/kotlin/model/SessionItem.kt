package model

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
class SessionItem(
    override val name: String,
    override val lastUsed: Long,
) : ListItem<SessionItem>, JavaSerializable {
    override fun usedTimeUpdated(usedTime: Long): SessionItem = SessionItem(name, usedTime)
}
