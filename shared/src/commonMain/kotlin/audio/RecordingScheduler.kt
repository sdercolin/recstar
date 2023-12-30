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
            var time = config?.startNode?.timeMs ?: 0
            state = State.RecordingStandby
            val startRecodingTime = config?.recordingStartNode?.timeMs
            if (startRecodingTime != null && settings.trim) {
                delay(startRecodingTime - time)
                time = startRecodingTime
            }
            state = State.Recording
            emitEvent(Event.StartRecording)
            val endRecordingTime = config?.recordingEndNode?.timeMs
            if (endRecordingTime != null && settings.trim) {
                delay(endRecordingTime - time)
                time = endRecordingTime
                state = State.RecordingStandby
                emitEvent(Event.StopRecording)
            }
            val switchTime = config?.switchingNode?.timeMs
            if (switchTime != null) {
                delay(switchTime - time)
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
