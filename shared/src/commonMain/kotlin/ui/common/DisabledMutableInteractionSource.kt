package ui.common

import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class DisabledMutableInteractionSource : MutableInteractionSource {
    override val interactions: Flow<Interaction>
        get() = flowOf()

    override suspend fun emit(interaction: Interaction) {
        // Do nothing
    }

    override fun tryEmit(interaction: Interaction): Boolean = false
}
