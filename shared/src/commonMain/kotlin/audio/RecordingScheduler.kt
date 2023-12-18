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
        StartRecording,
        StopRecording,
        Next,
        Stop,
    }

    var state = State.Idle
        private set

    enum class State {
        Idle,
        RecordingStandby,
        Recording,
        Switching,
        Stopping,
    }

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow: Flow<Event> = _eventFlow

    /**
     * Enter recording state with the given guide audio [config].
     */
    fun start(config: GuideAudio?) {
        if (state != State.Idle && state != State.Switching) {
            Log.e("RecordingScheduler start: illegal state $state")
            return
        }
        job?.cancel()
        job = scope.launch {
            Log.d("RecordingScheduler start: $config")
            var time = 0L
            state = State.RecordingStandby
            val startDelay = config?.recordingStartNode?.timeMs
            if (startDelay != null && settings.trim) {
                delay(startDelay)
                time = startDelay
            }
            state = State.Recording
            emitEvent(Event.StartRecording)
            val endDelay = config?.recordingEndNode?.timeMs
            if (endDelay != null && settings.trim) {
                delay(endDelay - time)
                time = endDelay
                state = State.RecordingStandby
                emitEvent(Event.StopRecording)
            }
            val switchDelay = config?.switchingNode?.timeMs
            if (switchDelay != null) {
                delay(switchDelay - time)
                if (settings.continuous) {
                    state = State.Switching
                    emitEvent(Event.Next)
                } else {
                    state = State.Stopping
                    emitEvent(Event.Stop)
                }
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
