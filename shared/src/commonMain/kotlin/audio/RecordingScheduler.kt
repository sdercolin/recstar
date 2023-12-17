package audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import model.GuideAudio
import repository.AppPreferenceRepository
import util.Log

/**
 * A class to handle scheduled events during recording process.
 */
class RecordingScheduler(
    private val appPreferenceRepository: AppPreferenceRepository,
    parentCoroutineScope: CoroutineScope,
) {
    private val settings get() = appPreferenceRepository.value.recording
    private val scope = CoroutineScope(parentCoroutineScope.coroutineContext + Dispatchers.Default)
    private var job: Job? = null

    enum class Event {
        Next,
        Stop,
    }

    var state = State.Idle
        private set

    enum class State {
        Idle,
        Recording,
        Switching,
        Stopping,
    }

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow: Flow<Event> = _eventFlow

    /**
     * Enter recording state with the given guide audio [config].
     */
    fun start(config: GuideAudio) {
        val switchingNode = config.switchingNode ?: return
        Log.d("RecordingScheduler start: switchingNode=$switchingNode")
        if (settings.continuous) {
            val repeatStartingNode = config.repeatStartingNode
            if (repeatStartingNode == null) {
                Log.w("Cannot find repeat starting node but switching node exists")
                return
            }
        }
        job?.cancel()
        job = scope.launch {
            state = State.Recording
            val delayMs = switchingNode.timeMs ?: return@launch
            delay(delayMs)
            if (settings.continuous) {
                state = State.Switching
                emitEvent(Event.Next)
            } else {
                state = State.Stopping
                emitEvent(Event.Stop)
            }
        }
    }

    /**
     * Called when the guide audio reaches the end before scheduled NEXT event.
     */
    fun onGuideAudioEnd() {
        if (state != State.Recording) return
        Log.d("RecordingScheduler onReachAudioEnd")
        job = scope.launch {
            if (settings.continuous) {
                state = State.Switching
                emitEvent(Event.Next)
            } else {
                state = State.Stopping
                emitEvent(Event.Stop)
            }
        }
    }

    /**
     * Finish the scheduler due to user action, or when all recordings are finished.
     */
    fun finish() {
        job?.cancel()
        job = null
        state = State.Idle
    }

    private suspend fun emitEvent(event: Event) {
        Log.d("RecordingScheduler emitEvent: $event")
        _eventFlow.emit(event)
    }

    fun dispose() {
        job?.cancel()
        job = null
    }
}
