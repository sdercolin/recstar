package audio

import android.media.MediaRecorder
import android.os.Build
import io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ui.model.AndroidContext
import ui.model.AppContext
import util.Log

class AudioRecorderImpl(
    private val listener: AudioRecorder.Listener,
    private val context: AndroidContext,
) : AudioRecorder {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var job: Job? = null
    private var cleanupJob: Job? = null
    private var recorder: MediaRecorder? = null

    override fun start(output: File) {
        val nativeContext = context.getAndroidNativeContext() ?: return
        if (job?.isActive == true) {
            Log.w("AudioRecorderImpl.start: already started")
            return
        }
        job = coroutineScope.launch(Dispatchers.IO) {
            cleanupJob?.join()
            cleanupJob = null
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(nativeContext)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            Log.i("AudioRecorderImpl.start: path: ${output.absolutePath}")
            this@AudioRecorderImpl.recorder = recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setAudioSamplingRate(44100)
                setAudioChannels(1)
                setOutputFile(output.absolutePath)
                prepare()
                start()
                withContext(Dispatchers.Main) {
                    listener.onStarted()
                }
            }
        }
    }

    override fun stop() {
        cleanupJob = coroutineScope.launch(Dispatchers.IO) {
            job?.cancelAndJoin()
            recorder?.stop()
            recorder?.release()
            recorder = null
            Log.i("AudioRecorderImpl.stop: stopped")
            withContext(Dispatchers.Main) {
                listener.onStopped()
            }
        }
    }

    override fun isRecording(): Boolean = recorder != null
}

actual class AudioRecorderProvider(
    private val listener: AudioRecorder.Listener,
    private val context: AndroidContext,
) {
    actual constructor(
        listener: AudioRecorder.Listener,
        context: AppContext,
    ) : this(listener, context as AndroidContext)

    actual fun get(): AudioRecorder = AudioRecorderImpl(listener, context)
}
