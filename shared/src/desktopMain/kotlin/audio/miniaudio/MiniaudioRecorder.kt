package audio.miniaudio

import audio.AudioFormat
import audio.AudioRecorder
import audio.PcmDecoder
import audio.WavData
import audio.WavWriter
import audio.getAudioFormat
import io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import model.AppPreference.AudioBackend
import repository.AppPreferenceRepository
import ui.common.ErrorNotifier
import util.Log
import util.runCatchingCancellable
import util.toJavaFile

internal class MiniaudioRecorder(
    private val backend: AudioBackend,
    private val listener: AudioRecorder.Listener,
    private val errorNotifier: ErrorNotifier,
    private val appPreferenceRepository: AppPreferenceRepository,
) : AudioRecorder {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var job: Job? = null
    private var session: Session? = null

    @Volatile
    private var disposed = false

    private val _waveDataFlow = MutableStateFlow<WavData>(arrayOf(FloatArray(0)))
    override val waveDataFlow: Flow<WavData> = _waveDataFlow

    private class Session {
        @Volatile
        var stopRequested = false

        @Volatile
        var isRecording = false
    }

    override fun start(output: File) {
        if (session?.stopRequested == false && job?.isActive == true) {
            Log.w("MiniaudioRecorder.start: already started")
            return
        }
        val previousJob = job
        val session = Session().also { this.session = it }
        job = scope.launch {
            previousJob?.join()
            if (session.stopRequested) return@launch
            runCatchingCancellable {
                _waveDataFlow.value = arrayOf(FloatArray(0))
                val preference = appPreferenceRepository.value
                val format = preference.getAudioFormat()
                val deviceName = withContext(Dispatchers.IO) {
                    MiniaudioEngine(backend).getInputDeviceInfos(preference.desiredInputName, format)
                        ?.selectedDeviceInfo
                        ?.takeUnless { it.isDefault || it.notFound }
                        ?.name
                }
                val capture = withContext(Dispatchers.IO) {
                    MiniaudioContext.get(backend).openCapture(deviceName, format, RING_BUFFER_MS)
                }
                Log.i("MiniaudioRecorder.start: path: ${output.absolutePath}, device: $deviceName, format: $format")
                try {
                    session.isRecording = true
                    listener.onStarted()
                    withContext(Dispatchers.IO) { record(capture, output, format, session) }
                } finally {
                    session.isRecording = false
                    withContext(NonCancellable + Dispatchers.IO) { capture.close() }
                    Log.i("MiniaudioRecorder: stopped")
                    if (!disposed) {
                        withContext(NonCancellable) { listener.onStopped() }
                    }
                }
            }.onFailure {
                errorNotifier.notify(it)
            }
        }
    }

    private suspend fun record(
        capture: MiniaudioCapture,
        output: File,
        format: AudioFormat,
        session: Session,
    ) {
        val decoder = PcmDecoder(format)
        val buffer = ByteArray(READ_FRAMES * capture.bytesPerFrame)
        var samples = FloatArray(format.sampleRate)
        var sampleCount = 0
        var lastEmitted = 0L
        var lastHeaderUpdate = System.currentTimeMillis()
        var reportedDroppedFrames = 0L

        WavWriter(output.toJavaFile(), format).use { writer ->
            capture.start()
            var stopping = false
            while (true) {
                if (!stopping && session.stopRequested) {
                    // Stopping first guarantees nothing is written after the final drain below.
                    capture.stop()
                    stopping = true
                }
                val frames = capture.read(buffer)
                if (frames > 0) {
                    writer.write(buffer, 0, frames * capture.bytesPerFrame)
                    if (sampleCount + frames > samples.size) {
                        samples = samples.copyOf(maxOf(samples.size * 2, sampleCount + frames))
                    }
                    decoder.decodeFirstChannel(buffer, frames, samples, sampleCount)
                    sampleCount += frames
                }
                if (stopping && frames == 0) break
                check(stopping || capture.isRunning) { "The audio input device stopped unexpectedly" }

                val now = System.currentTimeMillis()
                if (now - lastEmitted >= WAVEFORM_INTERVAL_MS) {
                    _waveDataFlow.value = Array(sampleCount) { floatArrayOf(samples[it]) }
                    lastEmitted = now
                }
                if (now - lastHeaderUpdate >= HEADER_UPDATE_INTERVAL_MS) {
                    writer.updateHeader()
                    lastHeaderUpdate = now
                }
                val droppedFrames = capture.droppedFrames
                if (droppedFrames != reportedDroppedFrames) {
                    Log.w("MiniaudioRecorder: dropped ${droppedFrames - reportedDroppedFrames} frames")
                    reportedDroppedFrames = droppedFrames
                }
                if (frames < READ_FRAMES) delay(POLL_INTERVAL_MS)
            }
        }
        _waveDataFlow.value = Array(sampleCount) { floatArrayOf(samples[it]) }
    }

    override fun stop() {
        session?.stopRequested = true
    }

    override fun isRecording(): Boolean = session?.isRecording == true

    override fun dispose() {
        disposed = true
        session?.stopRequested = true
        job?.cancel()
    }

    private companion object {
        const val RING_BUFFER_MS = 3000
        const val READ_FRAMES = 4096
        const val POLL_INTERVAL_MS = 10L
        const val WAVEFORM_INTERVAL_MS = 50L
        const val HEADER_UPDATE_INTERVAL_MS = 1000L
    }
}
