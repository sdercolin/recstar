package ui.common

import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable

class SnackbarBasedToastController {
    private val snackbarHostState = SnackbarHostState()

    suspend fun show(request: ToastRequest) {
        snackbarHostState.showSnackbar(request.message)
    }

    @Composable
    fun Compose() {
        SnackbarContainer(snackbarHostState)
    }
}
