package audio

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVLinearPCMBitDepthKey
import platform.AVFAudio.AVLinearPCMIsBigEndianKey
import platform.AVFAudio.AVLinearPCMIsFloatKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFAudio.setActive
import platform.CoreAudioTypes.kAudioFormatLinearPCM
import ui.model.AppContext
import util.Log
import util.withNSError
import util.withNSErrorCatching

@OptIn(ExperimentalForeignApi::class)
class AudioRecorderImpl(private val listener: AudioRecorder.Listener) : AudioRecorder {
    private var recorder: AVAudioRecorder? = null
    private var engine: AVAudioEngine? = null
    private var job: Job? = null
    private var cleanupJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun start(output: io.File) {
        runCatching {
            if (job?.isActive == true) {
                Log.w("AudioRecorderImpl.start: already started")
                return
            }
            waveData.clear()
            _waveDataFlow.value = FloatArray(0)
            job = scope.launch(Dispatchers.IO) {
                withNSError { e ->
                    val settings = mapOf<Any?, Any>(
                        AVFormatIDKey to kAudioFormatLinearPCM,
                        AVSampleRateKey to 44100.0,
                        AVNumberOfChannelsKey to 1,
                        AVLinearPCMBitDepthKey to 16,
                        AVLinearPCMIsBigEndianKey to false,
                        AVLinearPCMIsFloatKey to false,
                    )
                    val url = output.toNSURL()
                    Log.i("AudioRecorderImpl.start: url: $url")
                    recorder = AVAudioRecorder(url, settings, e)

                    val engine = AVAudioEngine()
                    this@AudioRecorderImpl.engine = engine
                    val input = engine.inputNode
                    val bus = 0L.toULong()
                    val format = input.inputFormatForBus(bus)
                    input.installTapOnBus(bus, 1792.toUInt(), format) { buffer, _ ->
                        if (buffer != null) {
                            addWaveData(buffer)
                        }
                    }
                    withNSError { e0 ->
                        engine.startAndReturnError(e0)
                    }
                    recorder?.record()
                    withContext(Dispatchers.Default) {
                        listener.onStarted()
                    }
                }
            }
        }.onFailure {
            Log.e(it)
            dispose()
        }
    }

    override fun stop() {
        runCatching {
            cleanupJob = scope.launch(Dispatchers.IO) {
                recorder?.stop()
                recorder = null
                job?.cancelAndJoin()
                engine?.stop()
                engine = null
                withNSErrorCatching { e ->
                    AVAudioSession.sharedInstance().setActive(false, e)
                }.onFailure {
                    Log.e("Failed to free AVAudioSession", it)
                }
                Log.i("AudioRecorderImpl.stop: stopped")
                withContext(Dispatchers.Main) {
                    listener.onStopped()
                }
            }
        }.onFailure {
            Log.e(it)
            dispose()
        }
    }

    override fun isRecording(): Boolean = recorder?.recording == true

    override fun dispose() {
        try {
            cleanupJob?.cancel()
            job?.cancel()
            recorder?.takeIf { it.recording }?.stop()
            recorder = null
            engine?.takeIf { it.running }?.stop()
            engine = null
        } catch (e: Exception) {
            Log.e(e)
        }
    }

    private fun addWaveData(buffer: AVAudioPCMBuffer) {
        val frameLength = buffer.frameLength.toInt()
        val channelData = buffer.floatChannelData?.get(0) ?: return
        for (i in 0 until frameLength) {
            waveData.add(channelData[i])
        }
        _waveDataFlow.value = waveData.toFloatArray()
    }

    private val waveData = mutableListOf<Float>()
    private val _waveDataFlow = MutableStateFlow(FloatArray(0))
    override val waveDataFlow: Flow<FloatArray> = _waveDataFlow
}

actual class AudioRecorderProvider actual constructor(
    private val listener: AudioRecorder.Listener,
    context: AppContext,
) {
    actual fun get(): AudioRecorder = AudioRecorderImpl(listener)
}
