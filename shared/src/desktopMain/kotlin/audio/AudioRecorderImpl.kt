package audio

import androidx.compose.runtime.Stable
import io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ui.model.AppContext
import util.toJavaFile
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.TargetDataLine

@Stable
class AudioRecorderImpl(private val listener: AudioRecorder.Listener) : AudioRecorder {
    private var line: TargetDataLine? = null
    private var job: Job? = null
    private var cleanupJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun start(output: File) {
        if (job?.isActive == true) {
            println("AudioRecorderImpl.start: already started")
            return
        }
        job = scope.launch {
            cleanupJob?.join()
            cleanupJob = null
            val format = AudioFormat(44100.0f, 16, 1, true, true)
            line = AudioSystem.getTargetDataLine(format).apply {
                open(format)
                start()
            }
            println("AudioRecorderImpl.start: path: ${output.absolutePath}")
            listener.onStarted()
            withContext(Dispatchers.IO) {
                AudioSystem.write(
                    AudioInputStream(line),
                    AudioFileFormat.Type.WAVE,
                    output.toJavaFile()
                )
            }
        }
    }

    override fun stop() {
        cleanupJob = scope.launch(Dispatchers.IO) {
            line?.stop()
            line?.flush()
            line?.close()
            job?.cancelAndJoin()
            line = null
            println("AudioRecorderImpl.stop: stopped")
            withContext(Dispatchers.Default) {
                listener.onStopped()
            }
        }
    }

    override fun isRecording(): Boolean = line?.isActive == true
}

actual class AudioRecorderProvider(private val listener: AudioRecorder.Listener) {

    actual constructor(listener: AudioRecorder.Listener, context: AppContext) : this(listener)

    actual fun get(): AudioRecorder = AudioRecorderImpl(listener)
}