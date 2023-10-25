package ui.common

import android.widget.Toast
import androidx.compose.runtime.Composable
import ui.model.AppContext
import ui.model.androidContext
import ui.model.androidNativeContext

actual class ToastController actual constructor(private val context: AppContext) {
    init {
        context.androidContext.toastController = this
    }

    actual fun show(request: ToastRequest) {
        val nativeContext = context.androidNativeContext
        Toast.makeText(
            nativeContext,
            request.message,
            when (request.duration) {
                ToastDuration.Short -> Toast.LENGTH_SHORT
                ToastDuration.Long -> Toast.LENGTH_LONG
            },
        ).show()
    }

    @Composable
    actual fun Compose() {
        // keep empty, because we are using Android's native Toast
    }
}
