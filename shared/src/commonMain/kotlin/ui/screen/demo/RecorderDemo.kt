package ui.screen.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import audio.AudioRecorder
import audio.AudioRecorderProvider
import io.File
import io.LocalFileInteractor
import io.LocalPermissionChecker
import io.Paths
import kotlinx.datetime.Clock
import repository.LocalAppPreferenceRepository
import ui.common.ErrorNotifier
import ui.common.LocalAlertDialogController
import ui.model.LocalAppContext
import ui.model.Screen

object RecorderDemoScreen : Screen {
    @Composable
    override fun getTitle(): String = "Recorder Demo"

    @Composable
    override fun Content() = RecorderDemo()
}

@Composable
private fun RecorderDemo() {
    var isRecording by remember { mutableStateOf(false) }
    var isRequestedRecording by remember { mutableStateOf(false) }
    val listener = remember {
        object : AudioRecorder.Listener {
            override fun onStarted() {
                isRecording = true
            }

            override fun onStopped() {
                isRecording = false
            }
        }
    }
    val context = LocalAppContext.current
    val fileInteractor = LocalFileInteractor.current
    val permissionChecker = LocalPermissionChecker.current
    val alertDialogController = LocalAlertDialogController.current
    val appPreferenceRepository = LocalAppPreferenceRepository.current
    val errorNotifier = remember(context, alertDialogController, fileInteractor) {
        ErrorNotifier(
            context = context,
            alertDialogController = alertDialogController,
            fileInteractor = fileInteractor,
            onFatalError = {},
        )
    }
    val recorder = remember {
        AudioRecorderProvider(listener, context, errorNotifier, appPreferenceRepository).get()
    }
    var isPermissionGranted by remember {
        mutableStateOf(permissionChecker.checkAndRequestRecordingPermission())
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = "Is recording: $isRecording")
            Button(
                enabled = isRecording == isRequestedRecording,
                onClick = {
                    val nextRequestedRecording = !isRequestedRecording
                    if (nextRequestedRecording) {
                        if (!isPermissionGranted) {
                            isPermissionGranted = permissionChecker.checkAndRequestRecordingPermission()
                        }
                        if (isPermissionGranted && nextRequestedRecording) {
                            isRequestedRecording = nextRequestedRecording
                            recorder.start(getOutputFile())
                        } else {
                            // Show toast
                        }
                    } else {
                        isRequestedRecording = nextRequestedRecording
                        recorder.stop()
                    }
                },
            ) {
                Text(text = if (isRecording) "Stop recording" else "Start recording")
            }
            Button(onClick = { fileInteractor.requestOpenFolder(Paths.contentRoot) }) {
                Text(text = "Show output directory")
            }
        }
    }
}

private fun getOutputFile(): File {
    val outputDir = Paths.contentRoot
    val dateTimeSuffix = Clock.System.now().epochSeconds.toString()
    return outputDir.resolve("record-test-$dateTimeSuffix.wav")
}
