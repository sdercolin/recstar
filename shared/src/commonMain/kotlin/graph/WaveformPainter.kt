package graph

import audio.WavReader
import io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ui.common.UnexpectedErrorNotifier

class WaveformPainter(
    private val recordingFlow: Flow<FloatArray>,
    private val coroutineScope: CoroutineScope,
    private val unexpectedErrorNotifier: UnexpectedErrorNotifier,
) {
    private var pointPerPixel = 60

    private val _flow = MutableStateFlow(emptyArray<FloatArray>())
    val flow: Flow<Array<FloatArray>> = _flow

    private val wavReader = WavReader()

    private var pendingFile: File? = null

    fun switch(file: File) {
        pendingFile = file
        if (job?.isActive == true) return
        consumePendingFile()
    }

    private fun consumePendingFile() {
        val file = pendingFile ?: return
        if (file.isFile) {
            runCatching {
                wavReader.read(file)
            }.onFailure {
                unexpectedErrorNotifier.notify(it)
            }.onSuccess {
                _flow.value = getSampledData(it)
            }
        } else {
            _flow.value = emptyArray()
        }
    }

    private var job: Job? = null

    fun onStartRecording() {
        job?.cancel()
        job = coroutineScope.launch(Dispatchers.Default) {
            recordingFlow.collect { data ->
                _flow.value = getSampledData(data)
            }
        }
    }

    fun clear() {
        job?.cancel()
        job = null
        _flow.value = emptyArray()
    }

    private fun getSampledData(data: FloatArray): Array<FloatArray> {
        val sampledData = Array(data.size / pointPerPixel) { FloatArray(2) }
        for (i in sampledData.indices) {
            var max = 0f
            var min = 0f
            for (j in 0 until pointPerPixel) {
                val index = i * pointPerPixel + j
                if (index >= data.size) break
                val value = data[index]
                if (value > max) max = value
                if (value < min) min = value
            }
            sampledData[i][0] = max
            sampledData[i][1] = min
        }
        return sampledData
    }

    fun onStopRecording(isSwitchingScheduled: Boolean) {
        coroutineScope.launch {
            job?.cancelAndJoin()
            job = null
            if (!isSwitchingScheduled) {
                consumePendingFile()
            }
        }
    }

    fun dispose() {
        job?.cancel()
        job = null
        pendingFile = null
    }
}
