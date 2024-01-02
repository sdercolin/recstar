package audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import model.GuideAudio
import repository.AppPreferenceRepository
import util.Log

/**
 * A class to handle scheduled events after recording process.
 */
class PostRecordingScheduler(
    private val appPreferenceRepository: AppPreferenceRepository,
    parentCoroutineScope: CoroutineScope,
) {
    private val settings get() = appPreferenceRepository.value.recording
    private val scope = CoroutineScope(parentCoroutineScope.coroutineContext + Dispatchers.Default)
    private var job: Job? = null

    enum class Event {
        Playback,
        Next,
    }

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow: Flow<Event> = _eventFlow

    private var isWaitingForPlayback = false

    /**
     * Called when recording is finished.
     */
    fun onFinishRecording(guideAudio: GuideAudio?) {
        if (settings.continuous && guideAudio != null) {
            // Disabled in continuous mode.
            return
        }
        if (settings.autoListenBack) {
            job?.cancel()
            job = scope.launch {
                emitEvent(Event.Playback)
            }
            isWaitingForPlayback = true
            return
        }
        if (settings.autoNext) {
            job?.cancel()
            job = scope.launch {
                emitEvent(Event.Next)
            }
            return
        }
    }

    /**
     * Called when playback is finished.
     */
    fun onFinishPlayback(guideAudio: GuideAudio?) {
        if (settings.continuous && guideAudio != null) {
            // Disabled in continuous mode.
            return
        }
        if (!isWaitingForPlayback) {
            return
        }
        if (settings.autoNext) {
            job?.cancel()
            job = scope.launch {
                emitEvent(Event.Next)
            }
            return
        }
    }

    private suspend fun emitEvent(event: Event) {
        Log.d("PostRecordingScheduler emitEvent: $event")
        _eventFlow.emit(event)
    }

    fun dispose() {
        job?.cancel()
        job = null
    }
}
