import androidx.compose.runtime.staticCompositionLocalOf
import io.Paths
import io.appRecordFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import model.AppRecord
import util.Log
import util.parseJson
import util.stringifyJson

class AppRecordStore(appRecord: AppRecord, private val scope: CoroutineScope) {
    private val _stateFlow = MutableStateFlow(appRecord)
    val stateFlow: StateFlow<AppRecord> = _stateFlow

    init {
        collectAndWrite()
    }

    private fun push(appRecord: AppRecord) {
        scope.launch(Dispatchers.IO) {
            _stateFlow.emit(appRecord)
        }
    }

    private fun collectAndWrite() {
        scope.launch(Dispatchers.IO) {
            _stateFlow.collectLatest {
                delay(THROTTLE_PERIOD_MS)
                Paths.appRecordFile.writeText(it.stringifyJson())
                Log.d("Written appRecord: $it")
            }
        }
    }

    val value get() = stateFlow.value

    fun update(updater: AppRecord.() -> AppRecord) {
        push(updater(value))
    }

    companion object {
        private const val THROTTLE_PERIOD_MS = 500L
    }
}

fun createAppRecordStore(scope: CoroutineScope): AppRecordStore {
    val recordText = Paths.appRecordFile.takeIf { it.exists() }?.readText()
    val appRecord = recordText?.parseJson() ?: AppRecord()
    return AppRecordStore(appRecord, scope)
}

val LocalAppRecordStore = staticCompositionLocalOf<AppRecordStore> {
    error("No AppRecordStore provided")
}
