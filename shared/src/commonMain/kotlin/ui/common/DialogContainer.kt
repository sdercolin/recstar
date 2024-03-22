package ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ui.style.CustomColors

@Composable
fun DialogContainer(
    fraction: Float,
    onClickOutside: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().background(color = CustomColors.Black50).plainClickable(onClickOutside),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(fraction).plainClickable(),
            shape = MaterialTheme.shapes.medium,
        ) {
            content()
        }
    }
}
