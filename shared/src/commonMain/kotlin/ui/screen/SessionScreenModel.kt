package ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import audio.AudioPlayer
import audio.AudioPlayerProvider
import audio.AudioRecorder
import audio.AudioRecorderProvider
import audio.RecordingScheduler
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import exception.SessionRenameExistingException
import exception.SessionRenameInvalidException
import graph.WaveformPainter
import io.File
import io.LocalPermissionChecker
import io.PermissionChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import model.GuideAudio
import model.Session
import repository.AppPreferenceRepository
import repository.LocalAppPreferenceRepository
import repository.LocalSessionRepository
import repository.SessionRepository
import ui.common.AlertDialogController
import ui.common.LocalAlertDialogController
import ui.common.requestConfirm
import ui.common.requestConfirmError
import ui.model.AppContext
import ui.model.LocalAppContext
import ui.model.Sentence
import ui.string.*
import util.Log
import util.savedMutableStateOf

class SessionScreenModel(
    session: Session,
    context: AppContext,
    appPreferenceRepository: AppPreferenceRepository,
    private val sessionRepository: SessionRepository,
    private val alertDialogController: AlertDialogController,
    private val permissionChecker: PermissionChecker,
) : ScreenModel {
    var name: String by mutableStateOf(session.name)
        private set

    var contentDirectory: File by mutableStateOf(session.directory)
        private set

    private val _sentences: SnapshotStateList<Sentence> = session.reclist.lines
        .map { Sentence(it, isFileExisting(it)) }.toMutableStateList()

    val sentences: List<Sentence> get() = _sentences

    var guideAudioConfig: GuideAudio? by mutableStateOf(session.guideAudioConfig)
        private set

    private val scheduler = RecordingScheduler(appPreferenceRepository, screenModelScope)

    var skipFinishedSentences: Boolean by savedMutableStateOf(session.skipFinishedSentence) {
        screenModelScope.launch {
            sessionRepository.update(sessionRepository.get(name).getOrThrow().copy(skipFinishedSentence = it))
        }
    }

    private fun reload(session: Session) {
        name = session.name
        contentDirectory = session.directory
        _sentences.clear()
        _sentences.addAll(session.reclist.lines.map { Sentence(it, isFileExisting(it)) })
        guideAudioConfig = session.guideAudioConfig
        currentIndex = currentIndex.coerceAtMost(_sentences.size - 1)
        Log.d("Reloading session: $name")
        Log.d("guideAudioConfig: $guideAudioConfig")
    }

    init {
        screenModelScope.launch {
            sessionRepository.sessionUpdated.collectLatest { updatedName ->
                if (updatedName == name) {
                    reload(sessionRepository.get(updatedName).getOrThrow())
                }
            }
        }
        screenModelScope.launch {
            scheduler.eventFlow.collectLatest { event ->
                when (event) {
                    RecordingScheduler.Event.Next -> nextWithScheduler()
                    RecordingScheduler.Event.Stop -> requestStopRecording()
                    RecordingScheduler.Event.StartRecording -> startRecording()
                    RecordingScheduler.Event.StopRecording -> stopRecording()
                }
            }
        }
    }

    var currentIndex: Int by savedMutableStateOf(0) { waveformPainter.switch(currentFile) }
        private set

    private var isRequestedRecording: Boolean by mutableStateOf(false)

    var isRecording: Boolean by mutableStateOf(false)
        private set

    var playingProgress: Float? by mutableStateOf(null)
        private set

    val isPlaying get() = playingProgress != null

    var isRequestedPlaying: Boolean by mutableStateOf(false)
        private set

    val isBusy: Boolean
        get() = isRequestedRecording != isRecording || isRequestedPlaying != isPlaying

    val currentSentence: Sentence
        get() = _sentences[currentIndex]

    private val playerListener = object : AudioPlayer.Listener {
        override fun onStarted() {
            // No-op
        }

        override fun onProgress(progress: Float) {
            playingProgress = progress
        }

        override fun onStopped() {
            playingProgress = null
            // this can be called without user's request
            isRequestedPlaying = false
        }
    }

    private val recorderListener = object : AudioRecorder.Listener {
        var waveformPainter: WaveformPainter? = null
        var lastSentenceIndex: Int? = null

        override fun onStarted() {
            isRecording = true
            lastSentenceIndex = currentIndex
        }

        override fun onStopped() {
            val state = scheduler.state
            Log.d("AudioRecorderListener onStopped, state: $state")
            val isSwitching = state == RecordingScheduler.State.Switching
            if (state != RecordingScheduler.State.RecordingStandby) {
                waveformPainter?.onStopRecording(isSwitching)
            }
            if (isSwitching) {
                switchScheduled()
            } else if (state == RecordingScheduler.State.Stopping || isRequestedRecording.not()) {
                isRecording = false
            }
            if (state != RecordingScheduler.State.RecordingStandby) {
                // delay update until switching or stopping
                lastSentenceIndex?.let { updateSentence(it) }
            }
        }
    }

    private val guidePlayerListener = object : AudioPlayer.Listener {
        override fun onStarted() {
            if (scheduler.state == RecordingScheduler.State.Switching || !isRecording) {
                startRecordingSchedule()
                waveformPainter.clear()
                isRecording = true
            }
        }

        override fun onProgress(progress: Float) {
            // No-op
        }

        override fun onStopped() {
            if (scheduler.state == RecordingScheduler.State.Recording) {
                scheduler.onGuideAudioEnd()
            }
        }
    }

    private val player = AudioPlayerProvider(playerListener, context).get()
    private val recorder = AudioRecorderProvider(recorderListener, context).get()
    private val waveformPainter = WaveformPainter(recorder.waveDataFlow, screenModelScope).apply {
        switch(currentFile)
        recorderListener.waveformPainter = this
    }

    val waveformFlow: Flow<Array<FloatArray>> = waveformPainter.flow

    private val guidePlayer = AudioPlayerProvider(guidePlayerListener, context).get()

    private var isPermissionGranted = permissionChecker.checkAndRequestRecordingPermission()

    private val _requestScrollToCurrentSentenceFlow = MutableSharedFlow<Unit>()
    val requestScrollToCurrentSentenceFlow: Flow<Unit> = _requestScrollToCurrentSentenceFlow

    private fun requestScrollToCurrentSentence() {
        screenModelScope.launch {
            _requestScrollToCurrentSentenceFlow.emit(Unit)
        }
    }

    private fun getFile(sentence: String): File {
        return contentDirectory.resolve("$sentence.wav")
    }

    private fun isFileExisting(sentence: String): Boolean {
        return getFile(sentence).isFile
    }

    private val currentFile: File
        get() = getFile(currentSentence.text)

    private fun updateCurrentSentence() {
        updateSentence(currentIndex)
    }

    private fun updateSentence(index: Int) {
        _sentences[index] = _sentences[index].copy(isFinished = isFileExisting(_sentences[index].text))
    }

    fun toggleRecording() {
        if (isRecording) {
            requestStopRecording()
        } else {
            requestStartRecording()
        }
    }

    private fun requestStartRecording() {
        if (!isPermissionGranted) {
            // We have already checked/requested the permission when this screen is shown.
            // If the permission is not granted, we try to request it once again.
            val isPermissionIgnored = permissionChecker.checkRecordingPermissionIgnored()
            if (isPermissionIgnored) {
                // Cannot request the permission again.
                alertDialogController.requestConfirm(
                    title = stringStatic(Strings.AlertNeedManualPermissionGrantTitle),
                    message = stringStatic(Strings.AlertNeedManualPermissionGrantMessage),
                )
                return
            }
            // Check/request the permission again.
            isPermissionGranted = permissionChecker.checkAndRequestRecordingPermission()
            if (!isPermissionGranted) {
                return
            }
        }
        val guideAudio = guideAudioConfig
        if (guideAudio != null) {
            if (guideAudio.getFile().exists().not()) {
                Log.d("Guide audio file not found: ${guideAudio.getFile().absolutePath}")
                alertDialogController.requestConfirm(
                    message = stringStatic(Strings.SessionScreenAlertGuideAudioNotFoundMessage),
                )
                return
            }
        }
        isRequestedRecording = true
        prepareOutputFile()
        if (guideAudio != null) {
            guidePlayer.play(guideAudio.getFile())
        } else {
            startRecordingSchedule()
        }
    }

    private fun startRecordingSchedule() {
        scheduler.start(guideAudioConfig)
    }

    private fun startRecording() {
        if (recorder.isRecording()) {
            Log.w("AudioRecorder is already recording")
            return
        }
        recorder.start(currentFile)
        waveformPainter.onStartRecording()
    }

    private fun prepareOutputFile() {
        currentFile.parentFile?.mkdirs()
        if (currentFile.exists()) {
            Log.i("Deleting existing file: $currentFile for recording")
            currentFile.delete()
        }
    }

    private fun requestStopRecording() {
        isRequestedRecording = false
        scheduler.finish()
        if (recorder.isRecording()) {
            stopRecording()
        } else {
            updateCurrentSentence()
            isRecording = false
        }
        if (guidePlayer.isPlaying()) {
            guidePlayer.stop()
        }
    }

    private fun stopRecording() {
        if (!recorder.isRecording()) {
            Log.w("AudioRecorder is not recording")
            return
        }
        recorder.stop()
    }

    fun togglePlaying() {
        if (isPlaying) {
            stopPlaying()
        } else {
            startPlaying()
        }
    }

    private fun startPlaying() {
        isRequestedPlaying = true
        player.play(currentFile)
    }

    private fun stopPlaying() {
        isRequestedPlaying = false
        player.stop()
    }

    fun selectSentence(
        index: Int,
        scheduled: Boolean = false,
    ) {
        if (index == currentIndex) return
        updateSentence(currentIndex)
        currentIndex = index
        if (scheduled) {
            // do no show stopped state on UI because it is scheduled and will continue soon
            if (recorder.isRecording()) {
                stopRecording()
            } else {
                switchScheduled()
            }
        } else {
            if (isRecording || isRequestedRecording) {
                requestStopRecording()
            } else {
                updateCurrentSentence()
            }
            if (isPlaying || isRequestedPlaying) {
                stopPlaying()
            }
        }
        requestScrollToCurrentSentence()
    }

    private fun getNextSentenceIndex(): Int? {
        val isLast = currentIndex == _sentences.size - 1
        if (isLast) return null
        if (!skipFinishedSentences) return currentIndex + 1
        _sentences.drop(currentIndex + 1).indexOfFirst { it.isFinished.not() }.let {
            if (it == -1) return null
            return currentIndex + 1 + it
        }
    }

    val hasNext: Boolean get() = getNextSentenceIndex() != null

    fun next() {
        val index = getNextSentenceIndex() ?: return
        selectSentence(index)
    }

    private fun nextWithScheduler() {
        val index = getNextSentenceIndex()
        if (index == null) {
            Log.d("SessionScreenModel nextWithScheduler: no next sentence")
            scheduler.finish()
            return
        }
        selectSentence(index, scheduled = true)
    }

    private fun switchScheduled() {
        val config = guideAudioConfig
        if (config != null) {
            config.repeatStartingNode?.timeMs?.let {
                guidePlayer.seekAndPlay(it)
                // expecting stopped -> started events from guidePlayerListener
            }
        } else {
            Log.e("SessionScreenModel switchScheduled: guideAudioConfig is null")
        }
    }

    private fun getPreviousSentenceIndex(): Int? {
        val isFirst = currentIndex == 0
        if (isFirst) return null
        if (!skipFinishedSentences) return currentIndex - 1
        return _sentences.take(currentIndex).indexOfLast { it.isFinished.not() }.takeIf { it != -1 }
    }

    val hasPrevious get() = getPreviousSentenceIndex() != null

    fun previous() {
        val index = getPreviousSentenceIndex() ?: return
        selectSentence(index)
    }

    fun renameSession(newName: String) {
        screenModelScope.launch(Dispatchers.IO) {
            runCatching {
                val newSession = sessionRepository.rename(name, newName).getOrThrow()
                reload(newSession)
            }.onFailure {
                withContext(Dispatchers.Main) {
                    Log.e("Failed to rename session", it)
                    when (it) {
                        is SessionRenameInvalidException,
                        is SessionRenameExistingException,
                        -> {
                            alertDialogController.requestConfirmError(
                                message = it.message,
                            )
                        }
                        else -> {
                            alertDialogController.requestConfirm(
                                message = stringStatic(Strings.ExceptionRenameSessionUnexpected),
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDispose() {
        scheduler.dispose()
        recorder.dispose()
        player.dispose()
        waveformPainter.dispose()
        super.onDispose()
    }
}

@Composable
fun SessionScreen.rememberSessionScreenModel(session: Session): SessionScreenModel {
    val context = LocalAppContext.current
    val appPreferenceRepository = LocalAppPreferenceRepository.current
    val alertDialogController = LocalAlertDialogController.current
    val permissionChecker = LocalPermissionChecker.current
    val sessionRepository = LocalSessionRepository.current
    return rememberScreenModel {
        SessionScreenModel(
            session = session,
            context = context,
            appPreferenceRepository = appPreferenceRepository,
            sessionRepository = sessionRepository,
            alertDialogController = alertDialogController,
            permissionChecker = permissionChecker,
        )
    }
}
