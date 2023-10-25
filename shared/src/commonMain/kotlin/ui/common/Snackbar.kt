package ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SnackbarContainer(hostState: SnackbarHostState) {
    Box(Modifier.fillMaxSize()) {
        SnackbarHost(
            hostState = hostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Snackbar(
                shape = MaterialTheme.shapes.medium,
                snackbarData = it,
                actionColor = MaterialTheme.colors.primary,
            )
        }
    }
}
