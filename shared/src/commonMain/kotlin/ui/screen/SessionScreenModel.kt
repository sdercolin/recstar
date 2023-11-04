package ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import audio.AudioRecorder
import audio.AudioRecorderProvider
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import model.Session
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

    val sentences: List<Sentence>
        get() = _sentences

    private fun reload(session: Session) {
        name = session.name
        contentDirectory = session.directory
        _sentences.clear()
        _sentences.addAll(session.reclist.lines.map { Sentence(it, isFileExisting(it)) })
        currentIndex = currentIndex.coerceAtMost(sentences.size - 1)
    }

    private val recorderListener = object : AudioRecorder.Listener {
        override fun onStarted() {
            isRecording = true
        }

        override fun onStopped() {
            isRecording = false
        }
    }

    private val recorder = AudioRecorderProvider(recorderListener, context).get()
    private val waveformPainter = WaveformPainter(recorder.waveDataFlow, screenModelScope)

    val waveformFlow: Flow<Array<FloatArray>> = waveformPainter.flow

    var currentIndex: Int by savedMutableStateOf(0) { waveformPainter.switch(currentFile) }
        private set

    private var isRequestedRecording: Boolean by mutableStateOf(false)

    val isBusy: Boolean
        get() = isRequestedRecording != isRecording

    var isRecording: Boolean by mutableStateOf(false)
        private set

    val currentSentence: Sentence
        get() = sentences[currentIndex]

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
        _sentences[currentIndex] = currentSentence.copy(isFinished = isFileExisting(currentSentence.text))
    }

    fun toggleRecording() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
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
        isRequestedRecording = true
        prepareOutputFile()
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

    private fun stopRecording() {
        isRequestedRecording = false
        recorder.stop()
        waveformPainter.onStopRecording()
        updateCurrentSentence()
    }

    fun selectSentence(index: Int) {
        currentIndex = index
        updateCurrentSentence()
        requestScrollToCurrentSentence()
    }

    val hasNext get() = currentIndex < sentences.size - 1

    fun next() {
        if (!hasNext) return
        if (isRecording) {
            stopRecording()
        }
        currentIndex++
        requestScrollToCurrentSentence()
    }

    val hasPrevious get() = currentIndex > 0

    fun previous() {
        if (!hasPrevious) return
        if (isRecording) {
            stopRecording()
        }
        currentIndex--
        requestScrollToCurrentSentence()
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
        recorder.dispose()
        super.onDispose()
    }
}

@Composable
fun SessionScreen.rememberSessionScreenModel(session: Session): SessionScreenModel {
    val context = LocalAppContext.current
    val alertDialogController = LocalAlertDialogController.current
    val permissionChecker = LocalPermissionChecker.current
    val sessionRepository = LocalSessionRepository.current
    return rememberScreenModel {
        SessionScreenModel(
            session = session,
            context = context,
            sessionRepository = sessionRepository,
            alertDialogController = alertDialogController,
            permissionChecker = permissionChecker,
        )
    }
}
