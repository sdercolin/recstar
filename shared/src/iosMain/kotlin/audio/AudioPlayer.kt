package audio

import io.File
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionAllowBluetooth
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeDefault
import platform.AVFAudio.setActive
import repository.AppPreferenceRepository
import ui.common.UnexpectedErrorNotifier
import ui.model.AppContext
import util.Log
import util.runCatchingCancellable
import util.withNSError

@OptIn(ExperimentalForeignApi::class)
class AudioPlayerImpl(
    private val listener: AudioPlayer.Listener,
    context: AppContext,
    private val unexpectedErrorNotifier: UnexpectedErrorNotifier,
    private val appPreferenceRepository: AppPreferenceRepository,
) : AudioPlayer {
    private val scope = context.coroutineScope
    private var audioPlayer: AVAudioPlayer? = null
    private var fileDuration: Double = 0.0
    private var job: Job? = null
    private var seekingJob: Job? = null
    private var cleanupJob: Job? = null
    private var countingJob: Job? = null
    private var lastLoadedFile: File? = null
    private var lastLoadedFileModified: Long? = null

    private val delegate = AVAudioPlayerDelegate {
        scope.launch(Dispatchers.Main) {
            stop()
        }
    }

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
                setupAudioSession()
                val lastModified = file.lastModified
                if (lastLoadedFile != file || lastLoadedFileModified != lastModified) {
                    val url = file.toNSURL()
                    withNSError { e ->
                        audioPlayer = AVAudioPlayer(contentsOfURL = url, error = e).apply {
                            delegate = this@AudioPlayerImpl.delegate
                            prepareToPlay()
                            setCurrentTime(positionMs / 1000.0)
                            play()
                            fileDuration = duration
                        }
                    }
                } else {
                    audioPlayer?.setCurrentTime(positionMs / 1000.0)
                    audioPlayer?.play()
                }
                lastLoadedFile = file
                lastLoadedFileModified = lastModified
                withContext(Dispatchers.Main) {
                    listener.onStarted()
                }
                startCounting()
            }.onFailure {
                unexpectedErrorNotifier.notify(it)
                dispose()
            }
        }
    }

    override fun seekAndPlay(positionMs: Long) {
        seekingJob = scope.launch(Dispatchers.IO) {
            runCatchingCancellable {
                if (isPlaying()) {
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
        cleanupJob = scope.launch(Dispatchers.IO) {
            runCatchingCancellable {
                audioPlayer?.takeIf { it.isPlaying() }?.pause()
                stopCounting()
                withContext(Dispatchers.Main) {
                    listener.onStopped()
                }
            }.onFailure {
                unexpectedErrorNotifier.notify(it)
                dispose()
            }
        }
    }

    override fun isPlaying(): Boolean = audioPlayer?.isPlaying() ?: false

    override fun dispose() {
        runCatching {
            stopCounting()
            audioPlayer?.takeIf { it.isPlaying() }?.stop()
            audioPlayer = null
            job?.cancel()
            job = null
            seekingJob?.cancel()
            seekingJob = null
            lastLoadedFile = null
            lastLoadedFileModified = null
        }.onFailure {
            unexpectedErrorNotifier.notify(it)
        }
    }

    private fun startCounting() {
        countingJob?.cancel()
        countingJob = scope.launch {
            while (isActive && isPlaying()) {
                val elapsedTime = audioPlayer?.currentTime ?: 0.0
                val progress = (elapsedTime / fileDuration).toFloat()
                listener.onProgress(progress)
                delay(5L)
            }
        }
    }

    private fun stopCounting() {
        countingJob?.cancel()
    }

    private fun setupAudioSession() {
        withNSError { e ->
            AVAudioSession.sharedInstance().setCategory(
                AVAudioSessionCategoryPlayAndRecord,
                mode = AVAudioSessionModeDefault,
                options = AVAudioSessionCategoryOptionAllowBluetooth,
                error = e,
            )
        }
        withNSError { e ->
            AVAudioSession.sharedInstance().setActive(true, error = e)
        }
    }
}

actual class AudioPlayerProvider actual constructor(
    private val listener: AudioPlayer.Listener,
    private val context: AppContext,
    private val unexpectedErrorNotifier: UnexpectedErrorNotifier,
    private val appPreferenceRepository: AppPreferenceRepository,
) {
    actual fun get(): AudioPlayer = AudioPlayerImpl(listener, context, unexpectedErrorNotifier, appPreferenceRepository)
}
