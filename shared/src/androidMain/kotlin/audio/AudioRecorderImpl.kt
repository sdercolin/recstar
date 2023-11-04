package audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ui.model.AndroidContext
import ui.model.AppContext
import ui.model.androidNativeContext
import util.Log

class AudioRecorderImpl(
    private val listener: AudioRecorder.Listener,
    private val context: AndroidContext,
) : AudioRecorder {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var job: Job? = null
    private var cleanupJob: Job? = null
    private var recorder: MediaRecorder? = null
    private var interceptJob: Job? = null
    private var audioRecord: AudioRecord? = null
    private val interceptSampleRate = 44100
    private val bufferSize = AudioRecord.getMinBufferSize(
        interceptSampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
    )

    override fun start(output: File) {
        val nativeContext = context.androidNativeContext
        if (job?.isActive == true) {
            Log.w("AudioRecorderImpl.start: already started")
            return
        }
        waveData.clear()
        _waveDataFlow.value = FloatArray(0)
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
                startIntercepting()
                withContext(Dispatchers.Main) {
                    listener.onStarted()
                }
            }
        }
    }

    override fun stop() {
        cleanupJob = coroutineScope.launch(Dispatchers.IO) {
            job?.cancel()
            interceptJob?.cancelAndJoin()
            job?.cancelAndJoin()
            recorder?.stop()
            recorder?.release()
            recorder = null
            audioRecord?.release()
            audioRecord = null
            Log.i("AudioRecorderImpl.stop: stopped")
            withContext(Dispatchers.Main) {
                listener.onStopped()
            }
        }
    }

    override fun isRecording(): Boolean = recorder != null

    @SuppressLint("MissingPermission")
    private fun startIntercepting() {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            interceptSampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
        )

        interceptJob = coroutineScope.launch(Dispatchers.Default) {
            val audioRecord = audioRecord ?: return@launch
            val audioBuffer = ShortArray(bufferSize)
            audioRecord.startRecording()

            while (isActive && this@AudioRecorderImpl.isRecording()) {
                val readResult = audioRecord.read(audioBuffer, 0, audioBuffer.size)
                if (readResult > 0) {
                    val validAudioData = audioBuffer.copyOfRange(0, readResult)
                    addWaveData(validAudioData)
                } else {
                    Log.w("Error reading audio data: $readResult")
                }
            }

            audioRecord.stop()
            audioRecord.release()
        }
    }

    override fun dispose() {
        job?.cancel()
        cleanupJob?.cancel()
        interceptJob?.cancel()
        recorder?.release()
        audioRecord?.release()
        job = null
        audioRecord = null
        cleanupJob = null
        recorder = null
    }

    private val waveData = mutableListOf<Float>()
    private val _waveDataFlow = MutableStateFlow(FloatArray(0))

    private fun addWaveData(buffer: ShortArray) {
        val value = buffer.map { it.toFloat() / Short.MAX_VALUE }
        waveData.addAll(value)
        _waveDataFlow.value = waveData.toFloatArray()
    }

    override val waveDataFlow: Flow<FloatArray> = _waveDataFlow
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
