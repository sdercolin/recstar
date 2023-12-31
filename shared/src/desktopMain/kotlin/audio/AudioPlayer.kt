package audio

import io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ui.common.UnexpectedErrorNotifier
import ui.model.AppContext
import util.Log
import util.runCatchingCancellable
import util.toJavaFile
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

class AudioPlayerImpl(
    private val listener: AudioPlayer.Listener,
    context: AppContext,
    private val unexpectedErrorNotifier: UnexpectedErrorNotifier,
) : AudioPlayer {
    private val scope = context.coroutineScope
    private lateinit var clip: Clip
    private var lastLoadedFile: File? = null
    private var lastLoadedFileModified: Long? = null
    private var job: Job? = null
    private var seekingJob: Job? = null
    private var cleanupJob: Job? = null
    private var countingJob: Job? = null
    private var isPlaying = false
    private val initJob: Job = scope.launch(Dispatchers.IO) {
        clip = AudioSystem.getClip()
    }

    override fun play(
        file: File,
        positionMs: Long,
    ) {
        if (job?.isActive == true) {
            Log.w("AudioPlayerImpl.start: already started")
            return
        }
        job = scope.launch {
            if (initJob.isActive) {
                initJob.join()
            }
            runCatchingCancellable {
                cleanupJob?.join()
                cleanupJob = null
                withContext(Dispatchers.IO) {
                    if (lastLoadedFile != file || lastLoadedFileModified != file.lastModified) {
                        clip.close()
                        val audioInputStream = AudioSystem.getAudioInputStream(file.toJavaFile())
                        clip.open(audioInputStream)
                    }
                    clip.microsecondPosition = positionMs
                    clip.start()
                    startCounting()
                    lastLoadedFile = file
                    lastLoadedFileModified = file.lastModified
                    listener.onStarted()
                    isPlaying = true
                }
            }.onFailure {
                unexpectedErrorNotifier.notify(it)
                dispose()
            }
        }
    }

    override fun seekAndPlay(positionMs: Long) {
        seekingJob = scope.launch {
            runCatchingCancellable {
                if (isPlaying) {
                    stop()
                }
                cleanupJob?.join()
                play(requireNotNull(lastLoadedFile), positionMs)
            }.onFailure {
                unexpectedErrorNotifier.notify(it)
                dispose()
            }
        }
    }

    override fun stop() {
        listener.onStopped()
        cleanupJob = scope.launch(Dispatchers.IO) {
            runCatchingCancellable {
                job?.cancelAndJoin()
                countingJob?.cancelAndJoin()
                clip.apply {
                    stop()
                    flush()
                }
            }.onFailure {
                unexpectedErrorNotifier.notify(it)
                dispose()
            }
        }
    }

    override fun isPlaying(): Boolean = isPlaying

    private fun startCounting() {
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
    }

    override fun dispose() {
        runCatching {
            job?.cancel()
            seekingJob?.cancel()
            cleanupJob?.cancel()
            countingJob?.cancel()
            clip.close()
            lastLoadedFile = null
            lastLoadedFileModified = null
        }.onFailure {
            unexpectedErrorNotifier.notify(it)
        }
    }
}

actual class AudioPlayerProvider actual constructor(
    private val listener: AudioPlayer.Listener,
    private val context: AppContext,
    private val unexpectedErrorNotifier: UnexpectedErrorNotifier,
) {
    actual fun get(): AudioPlayer = AudioPlayerImpl(listener, context, unexpectedErrorNotifier)
}
