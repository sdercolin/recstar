package model

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
class GuideAudioItem(
    override val name: String,
    override val lastUsed: Long,
) : ListItem<GuideAudioItem>, JavaSerializable {
    override fun usedTimeUpdated(usedTime: Long): GuideAudioItem = GuideAudioItem(name, usedTime)
}
