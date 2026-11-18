package audio.miniaudio

import audio.AudioPlayer
import audio.getAudioFormat
import io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import model.AppPreference.AudioBackend
import repository.AppPreferenceRepository
import ui.common.ErrorNotifier
import ui.model.AppContext
import util.Log
import util.runCatchingCancellable

internal class MiniaudioPlayer(
    private val backend: AudioBackend,
    private val listener: AudioPlayer.Listener,
    context: AppContext,
    private val errorNotifier: ErrorNotifier,
    private val appPreferenceRepository: AppPreferenceRepository,
) : AudioPlayer {
    private val scope = context.coroutineScope
    private var playback: MiniaudioPlayback? = null
    private var openedDeviceName: String? = null
    private var lastLoadedFile: File? = null
    private var lastLoadedFileModified: Long? = null
    private var job: Job? = null
    private var countingJob: Job? = null
    private var volume = 1f

    @Volatile
    private var isPlaying = false

    override fun play(
        file: File,
        positionMs: Long,
    ) {
        if (job?.isActive == true) {
            Log.w("MiniaudioPlayer.play: already started")
            return
        }
        val previousCountingJob = countingJob
        job = scope.launch {
            runCatchingCancellable {
                previousCountingJob?.cancelAndJoin()
                val playback = withContext(Dispatchers.IO) {
                    val playback = getPlayback()
                    if (lastLoadedFile != file || lastLoadedFileModified != file.lastModified) {
                        playback.load(file.absolutePath)
                        ensureActive()
                        lastLoadedFile = file
                        lastLoadedFileModified = file.lastModified
                    }
                    playback.setVolume(volume)
                    playback.play(positionMs)
                    playback
                }
                isPlaying = true
                withContext(Dispatchers.Main) { listener.onStarted() }
                countingJob = scope.launch { count(playback) }
            }.onFailure {
                errorNotifier.notify(it)
                dispose()
            }
        }
    }

    private suspend fun getPlayback(): MiniaudioPlayback {
        val preference = appPreferenceRepository.value
        val deviceName = MiniaudioEngine(
            backend,
        ).getOutputDeviceInfos(preference.desiredOutputName, preference.getAudioFormat())
            ?.selectedDeviceInfo
            ?.takeUnless { it.isDefault || it.notFound }
            ?.name
        playback?.let { current ->
            if (deviceName == openedDeviceName) return current
            current.close()
            playback = null
            lastLoadedFile = null
            lastLoadedFileModified = null
        }
        return MiniaudioContext.get(backend).openPlayback(deviceName).also {
            playback = it
            openedDeviceName = deviceName
        }
    }

    private suspend fun count(playback: MiniaudioPlayback) {
        try {
            withContext(Dispatchers.IO) {
                while (playback.isPlaying) {
                    val progress = playback.progress
                    withContext(Dispatchers.Main) { listener.onProgress(progress) }
                    delay(PROGRESS_INTERVAL_MS)
                }
            }
        } finally {
            isPlaying = false
            withContext(NonCancellable + Dispatchers.IO) { playback.stop() }
            withContext(NonCancellable + Dispatchers.Main) { listener.onStopped() }
        }
    }

    override fun seekAndPlay(positionMs: Long) {
        scope.launch {
            runCatchingCancellable {
                job?.join()
                withContext(Dispatchers.IO) { playback?.stop() }
                countingJob?.join()
                play(requireNotNull(lastLoadedFile), positionMs)
            }.onFailure {
                errorNotifier.notify(it)
                dispose()
            }
        }
    }

    override fun stop() {
        scope.launch(Dispatchers.IO) {
            job?.join()
            playback?.stop()
        }
    }

    override fun isPlaying(): Boolean = isPlaying

    override fun setVolume(volume: Float) {
        this.volume = volume
        playback?.setVolume(volume)
    }

    override fun dispose() {
        runCatching {
            job?.cancel()
            countingJob?.cancel()
            playback?.close()
            playback = null
            openedDeviceName = null
            lastLoadedFile = null
            lastLoadedFileModified = null
        }.onFailure {
            errorNotifier.notify(it)
        }
    }

    private companion object {
        const val PROGRESS_INTERVAL_MS = 16L
    }
}
