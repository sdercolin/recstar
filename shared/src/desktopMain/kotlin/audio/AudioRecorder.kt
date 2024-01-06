package audio

import androidx.compose.runtime.Stable
import const.WavFormat
import io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ui.common.UnexpectedErrorNotifier
import ui.model.AppContext
import util.Log
import util.runCatchingCancellable
import util.toJavaFile
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.TargetDataLine

@Stable
class AudioRecorderImpl(
    private val listener: AudioRecorder.Listener,
    private val unexpectedErrorNotifier: UnexpectedErrorNotifier,
) : AudioRecorder {
    private var line: TargetDataLine? = null
    private var job: Job? = null
    private var cleanupJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var stream: InterceptedAudioInputStream? = null

    override fun start(output: File) {
        if (job?.isActive == true) {
            Log.w("AudioRecorderImpl.start: already started")
            return
        }
        job = scope.launch {
            runCatchingCancellable {
                cleanupJob?.join()
                cleanupJob = null
                _waveDataFlow.value = FloatArray(0)
                val format = AudioFormat(
                    WavFormat.SAMPLE_RATE.toFloat(),
                    WavFormat.BITS_PER_SAMPLE,
                    WavFormat.CHANNELS,
                    // signed
                    true,
                    // little endian
                    false,
                )
                val line = AudioSystem.getTargetDataLine(format).apply {
                    open(format)
                    start()
                }
                this@AudioRecorderImpl.line = line
                Log.i("AudioRecorderImpl.start: path: ${output.absolutePath}")
                listener.onStarted()
                withContext(Dispatchers.IO) {
                    val stream = InterceptedAudioInputStream(line, 1792, _waveDataFlow)
                    this@AudioRecorderImpl.stream = stream
                    AudioSystem.write(
                        AudioInputStream(stream, format, AudioSystem.NOT_SPECIFIED.toLong()),
                        AudioFileFormat.Type.WAVE,
                        output.toJavaFile(),
                    )
                }
            }.onFailure {
                unexpectedErrorNotifier.notify(it)
                dispose()
            }
        }
    }

    override fun stop() {
        cleanupJob = scope.launch(Dispatchers.IO) {
            runCatchingCancellable {
                line?.stop()
                line?.flush()
                line?.close()
                stream?.close()
                stream = null
                job?.cancelAndJoin()
                line = null
                Log.i("AudioRecorderImpl.stop: stopped")
                withContext(Dispatchers.Main) {
                    listener.onStopped()
                }
            }.onFailure {
                unexpectedErrorNotifier.notify(it)
                dispose()
            }
        }
    }

    override fun isRecording(): Boolean = line?.isActive == true

    override fun dispose() {
        runCatching {
            cleanupJob?.cancel()
            job?.cancel()
            line?.stop()
            line?.flush()
            line?.close()
            line = null
        }.onFailure {
            unexpectedErrorNotifier.notify(it)
        }
    }

    private val _waveDataFlow = MutableStateFlow(FloatArray(0))
    override val waveDataFlow: Flow<FloatArray> = _waveDataFlow
}

actual class AudioRecorderProvider(
    private val listener: AudioRecorder.Listener,
    private val unexpectedErrorNotifier: UnexpectedErrorNotifier,
) {
    actual constructor(
        listener: AudioRecorder.Listener,
        context: AppContext,
        unexpectedErrorNotifier: UnexpectedErrorNotifier,
    ) : this(listener, unexpectedErrorNotifier)

    actual fun get(): AudioRecorder = AudioRecorderImpl(listener, unexpectedErrorNotifier)
}
