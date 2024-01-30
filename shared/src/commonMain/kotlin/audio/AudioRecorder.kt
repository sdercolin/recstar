package audio

import io.File
import kotlinx.coroutines.flow.Flow
import repository.AppPreferenceRepository
import ui.common.UnexpectedErrorNotifier
import ui.model.AppContext

/**
 * An interface for recording audio.
 */
interface AudioRecorder {
    /**
     * A listener for [AudioRecorder] events.
     */
    interface Listener {
        fun onStarted()

        fun onStopped()
    }

    /**
     * An asynchronous operation that starts the recording and writes the output to the given [output].
     */
    fun start(output: File)

    /**
     * An asynchronous operation that stops the recording.
     */
    fun stop()

    /**
     * Returns `true` if the recorder is currently recording.
     */
    fun isRecording(): Boolean

    /**
     * Disposes of the resources used by the recorder.
     */
    fun dispose()

    /**
     * A [Flow] of the recorded [WavData].
     */
    val waveDataFlow: Flow<WavData>
}

expect class AudioRecorderProvider(
    listener: AudioRecorder.Listener,
    context: AppContext,
    unexpectedErrorNotifier: UnexpectedErrorNotifier,
    appPreferenceRepository: AppPreferenceRepository,
) {
    fun get(): AudioRecorder
}
