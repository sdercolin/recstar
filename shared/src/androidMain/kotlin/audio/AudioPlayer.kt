package audio

import android.media.MediaPlayer
import io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ui.model.AppContext
import util.Log
import util.runCatchingCancellable

class AudioPlayerImpl(private val listener: AudioPlayer.Listener, context: AppContext) : AudioPlayer {
    private val scope = context.coroutineScope
    private var mediaPlayer: MediaPlayer? = null
    private var lastLoadedFile: String? = null
    private var lastLoadedFileModified: Long? = null
    private var job: Job? = null
    private var countingJob: Job? = null
    private var startTime: Long = 0

    override fun play(
        file: File,
        positionMs: Long,
    ) {
        if (job?.isActive == true) {
            Log.w("AudioRecorderImpl.start: already started")
            return
        }
        job = scope.launch(Dispatchers.IO) {
            runCatchingCancellable {
                if (file.absolutePath == lastLoadedFile &&
                    lastLoadedFileModified == file.lastModified &&
                    mediaPlayer != null
                ) {
                    mediaPlayer?.seekTo(positionMs.toInt())
                    mediaPlayer?.start()
                    listener.onStarted()
                    startCounting()
                    return@launch
                }
                dispose()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(file.absolutePath)
                    prepareAsync()
                    setOnPreparedListener {
                        seekTo(positionMs.toInt())
                        start()
                        listener.onStarted()
                        startCounting()
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e("MediaPlayer error: what: $what, extra: $extra")
                        false
                    }
                    setOnCompletionListener {
                        listener.onStopped()
                        stopCounting()
                    }
                }
                lastLoadedFile = file.absolutePath
                lastLoadedFileModified = file.lastModified
            }.onFailure {
                Log.e(it)
                dispose()
            }
        }
    }

    override fun seekAndPlay(positionMs: Long) {
        if (isPlaying()) {
            stop()
        }
        play(File(requireNotNull(lastLoadedFile)), positionMs)
    }

    override fun stop() {
        scope.launch(Dispatchers.IO) {
            runCatchingCancellable {
                job?.cancelAndJoin()
                mediaPlayer?.pause()
                listener.onStopped()
                stopCounting()
            }.onFailure {
                Log.e(it)
                dispose()
            }
        }
    }

    override fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    override fun dispose() {
        runCatching {
            job?.cancel()
            stopCounting()
            mediaPlayer?.release()
            mediaPlayer = null
            lastLoadedFile = null
            lastLoadedFileModified = null
        }.onFailure { Log.e(it) }
    }

    private fun startCounting() {
        countingJob?.cancel()
        countingJob = scope.launch(Dispatchers.IO) {
            val mediaPlayer = mediaPlayer
            startTime = System.currentTimeMillis()
            while (isActive && mediaPlayer?.isPlaying == true) {
                val elapsedTime = System.currentTimeMillis() - startTime
                val progress = elapsedTime.toFloat() / mediaPlayer.duration
                withContext(Dispatchers.Main) {
                    listener.onProgress(progress)
                }
                delay(5)
            }
        }
    }

    private fun stopCounting() {
        countingJob?.cancel()
    }
}

actual class AudioPlayerProvider actual constructor(
    private val listener: AudioPlayer.Listener,
    private val context: AppContext,
) {
    actual fun get(): AudioPlayer = AudioPlayerImpl(listener, context)
}
