package audio

import io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ui.model.AppContext
import util.Log
import util.toJavaFile
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.SourceDataLine

class AudioPlayerImpl(private val listener: AudioPlayer.Listener, context: AppContext) : AudioPlayer {
    private val scope = context.coroutineScope
    private var line: SourceDataLine? = null
    private lateinit var clip: Clip
    private var job: Job? = null
    private var cleanupJob: Job? = null
    private var countingJob: Job? = null
    private var isPlaying = false
    private val initJob: Job = scope.launch(Dispatchers.IO) {
        clip = AudioSystem.getClip()
    }

    override fun play(file: File) {
        runCatching {
            if (job?.isActive == true) {
                Log.w("AudioPlayerImpl.start: already started")
                return
            }
            job = scope.launch {
                if (initJob.isActive) {
                    initJob.join()
                }
                cleanupJob?.join()
                cleanupJob = null
                withContext(Dispatchers.IO) {
                    val audioInputStream = AudioSystem.getAudioInputStream(file.toJavaFile())
                    clip.open(audioInputStream)
                    clip.start()
                    countingJob = scope.launch {
                        while (clip.isRunning) {
                            val progress = clip.microsecondPosition.toFloat() / clip.microsecondLength.toFloat()
                            withContext(Dispatchers.Main) {
                                listener.onProgress(progress)
                            }
                            delay(5)
                        }
                        stop()
                    }
                    listener.onStarted()
                    isPlaying = true
                }
            }
        }.onFailure {
            Log.e(it)
            dispose()
        }
    }

    override fun stop() {
        runCatching {
            listener.onStopped()
            cleanupJob = scope.launch(Dispatchers.IO) {
                job?.cancelAndJoin()
                countingJob?.cancelAndJoin()
                line?.stop()
                clip.stop()
                line?.flush()
                clip.flush()
                line?.close()
                clip.microsecondPosition = 0
                clip.close()
                line = null
            }
        }.onFailure {
            Log.e(it)
            dispose()
        }
    }

    override fun isPlaying(): Boolean = isPlaying

    override fun dispose() {
        runCatching {
            job?.cancel()
            cleanupJob?.cancel()
            countingJob?.cancel()
            clip.close()
            line?.close()
            line = null
        }.onFailure {
            Log.e(it)
        }
    }
}

actual class AudioPlayerProvider actual constructor(
    private val listener: AudioPlayer.Listener,
    private val context: AppContext,
) {
    actual fun get(): AudioPlayer = AudioPlayerImpl(listener, context)
}
