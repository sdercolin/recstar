package ui.encoding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import io.File
import kotlinx.coroutines.suspendCancellableCoroutine
import util.Encoding
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class TextEncodingDialogController {
    private val state = mutableStateOf<TextEncodingDialogRequest?>(null)
    private var continuation: Continuation<TextEncodingDialogResult?>? = null

    suspend fun await(request: TextEncodingDialogRequest) =
        suspendCancellableCoroutine { continuation ->
            state.value = request
            this.continuation = continuation
        }

    private fun dismiss() {
        state.value = null
        continuation = null
    }

    @Composable
    fun Compose() {
        val request = state.value
        if (request != null) {
            TextEncodingDialog(
                request,
                submit = { encoding ->
                    continuation?.resume(TextEncodingDialogResult(encoding))
                    dismiss()
                },
                dismiss = {
                    continuation?.resume(null)
                    dismiss()
                },
            )
        }
    }
}

val LocalTextEncodingDialogController = staticCompositionLocalOf<TextEncodingDialogController> {
    error("No TextEncodingDialogController provided!")
}

class TextEncodingDialogRequest(
    val file: File,
    val currentEncoding: Encoding? = null,
)

class TextEncodingDialogResult(
    // null to use auto-detect
    val encoding: Encoding?,
)
