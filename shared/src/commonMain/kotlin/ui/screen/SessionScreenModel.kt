package ui.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import audio.AudioRecorder
import audio.AudioRecorderProvider
import cafe.adriel.voyager.core.model.ScreenModel
import io.File
import ui.common.AlertDialogController
import ui.common.requestConfirm
import ui.model.AppContext
import ui.model.Sentence
import ui.string.Strings
import ui.string.stringStatic

class SessionScreenModel(
    sentences: List<String>,
    private val contentDirectory: File,
    private val context: AppContext,
    private val alertDialogController: AlertDialogController,
) : ScreenModel {
    private val _sentences: MutableList<Sentence> =
        sentences.map { Sentence(it, isFileExisting(it)) }.toMutableStateList()

    val sentences: List<Sentence>
        get() = _sentences

    private val recorderListener = object : AudioRecorder.Listener {
        override fun onStarted() {
            isRecording = true
        }

        override fun onStopped() {
            isRecording = false
        }
    }

    private val recorder = AudioRecorderProvider(recorderListener, context).get()

    var currentIndex: Int by mutableStateOf(0)
        private set

    private var isRequestedRecording: Boolean by mutableStateOf(false)

    private val isBusy: Boolean
        get() = isRequestedRecording != isRecording

    var isRecording: Boolean by mutableStateOf(false)
        private set

    val currentSentence: Sentence
        get() = sentences[currentIndex]

    var isPermissionGranted by mutableStateOf(context.checkAndRequestRecordingPermission())

    private fun getFile(sentence: String): File {
        return contentDirectory.resolve("$sentence.wav")
    }

    private fun isFileExisting(sentence: String): Boolean {
        return getFile(sentence).isFile
    }

    val currentFile: File
        get() = getFile(currentSentence.text)

    private fun updateCurrentSentence() {
        _sentences[currentIndex] = currentSentence.copy(isFinished = isFileExisting(currentSentence.text))
    }

    fun startRecording() {
        if (!isPermissionGranted) {
            // We have already checked/requested the permission when this screen is shown.
            // If the permission is not granted, we try to request it once again.
            val isPermissionIgnored = context.checkRecordingPermissionIgnored()
            if (isPermissionIgnored) {
                // Cannot request the permission again.
                alertDialogController.requestConfirm(
                    title = stringStatic(Strings.AlertNeedManualPermissionGrantTitle),
                    message = stringStatic(Strings.AlertNeedManualPermissionGrantMessage),
                )
                return
            }
            // Request the permission again.
            isPermissionGranted = context.checkAndRequestRecordingPermission()
            return
        }
        isRequestedRecording = true
        recorder.start(currentFile)
    }

    fun stopRecording() {
        isRequestedRecording = false
        recorder.stop()
        updateCurrentSentence()
    }

    fun selectSentence(index: Int) {
        currentIndex = index
        updateCurrentSentence()
    }

    fun hasNext() = currentIndex < sentences.size - 1

    fun next() {
        if (!hasNext()) return
        if (isRecording) {
            stopRecording()
        }
        currentIndex++
    }

    fun hasPrevious() = currentIndex > 0

    fun previous() {
        if (!hasPrevious()) return
        if (isRecording) {
            stopRecording()
        }
        currentIndex--
    }

    override fun onDispose() {
        recorder.dispose()
        super.onDispose()
    }
}
