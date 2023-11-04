package ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ui.style.CustomColors
import util.runIf

/**
 * An app-wide controller for showing progress.
 */
class ProgressController {
    private var current: ProgressRequest? by mutableStateOf(null)

    fun show(darkenBackground: Boolean = true) {
        current = ProgressRequest(darkenBackground)
    }

    fun hide() {
        current = null
    }

    @Composable
    fun Compose() {
        val current = this.current
        if (current != null) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .runIf(current.darkenBackground) { background(color = CustomColors.Black50) }
                    .plainClickable(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

data class ProgressRequest(
    val darkenBackground: Boolean,
)

val LocalProgressController = staticCompositionLocalOf<ProgressController> {
    error("No ProgressController provided!")
}
