package audio

import io.File
import ui.model.AppContext

interface AudioRecorder {

    interface Listener {
        fun onStarted()
        fun onStopped()
    }

    fun start(output: File)
    fun stop()
    fun isRecording(): Boolean
}

expect class AudioRecorderProvider(listener: AudioRecorder.Listener, context: AppContext) {
    fun get(): AudioRecorder
}
