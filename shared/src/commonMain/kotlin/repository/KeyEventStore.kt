package repository

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import util.isDesktop

/**
 * A store to collect and dispatch global key events on Desktop.
 */
class KeyEventStore(
    private val coroutineScope: CoroutineScope,
    private val appPreferenceRepository: AppPreferenceRepository,
) {
    private val _flow = MutableSharedFlow<KeyEvent>(replay = 0)
    val flow: SharedFlow<KeyEvent> = _flow

    private val keys = mutableSetOf<Key>()

    init {
        if (isDesktop) {
            coroutineScope.launch {
                appPreferenceRepository.flow.collectLatest {
                    keys.clear()
                    if (it.recording.recordWhileHolding) {
                        keys.add(it.recording.recordingShortKey.getKey())
                    }
                }
            }
        }
    }

    fun dispatch(keyEvent: KeyEvent): Boolean {
        if (keyEvent.key !in keys) {
            return false
        }
        coroutineScope.launch {
            _flow.emit(keyEvent)
        }
        return true
    }
}

val LocalKeyEventStore = staticCompositionLocalOf<KeyEventStore> {
    error("No KeyEventStore provided")
}
