package audio

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private var job: Job? = null
    private var cleanupJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun start(output: io.File) {
        if (job?.isActive == true) {
            Log.w("AudioRecorderImpl.start: already started")
            return
        }
        job = scope.launch {
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
                recorder?.record()
                withContext(Dispatchers.Default) {
                    listener.onStarted()
                }
            }
        }
    }

    override fun stop() {
        cleanupJob = scope.launch(Dispatchers.IO) {
            job?.cancelAndJoin()
            recorder?.stop()
            recorder = null
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
    }

    override fun isRecording(): Boolean = recorder?.recording == true

    override fun dispose() {
        cleanupJob?.cancel()
        job?.cancel()
        recorder?.stop()
        recorder = null
    }
}

actual class AudioRecorderProvider actual constructor(
    private val listener: AudioRecorder.Listener,
    context: AppContext,
) {
    actual fun get(): AudioRecorder = AudioRecorderImpl(listener)
}
