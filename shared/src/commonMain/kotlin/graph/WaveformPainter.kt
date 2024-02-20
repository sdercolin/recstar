package graph

import audio.WavData
import audio.WavReader
import io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ui.common.ErrorNotifier

class WaveformPainter(
    private val recordingFlow: Flow<WavData>,
    private val coroutineScope: CoroutineScope,
    private val errorNotifier: ErrorNotifier,
) {
    private var pointPerPixel = 60

    // Layers: [channel][point][max/min]
    private val _flow = MutableStateFlow(emptyArray<Array<FloatArray>>())
    val flow: Flow<Array<Array<FloatArray>>> = _flow

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
                errorNotifier.notify(it)
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

    private fun getSampledData(data: WavData): Array<Array<FloatArray>> {
        val channelSize = data.firstOrNull()?.size ?: return emptyArray()
        val result = Array(channelSize) { mutableListOf<FloatArray>() }

        for (channelIndex in result.indices) {
            val sampledData = Array(data.size / pointPerPixel) { FloatArray(2) }
            for (frameIndex in sampledData.indices) {
                var max = 0f
                var min = 0f
                for (j in 0 until pointPerPixel) {
                    val index = frameIndex * pointPerPixel + j
                    if (index >= data.size) break
                    val value = data[index][channelIndex]
                    if (value > max) max = value
                    if (value < min) min = value
                }
                sampledData[frameIndex][0] = max
                sampledData[frameIndex][1] = min
            }
            result[channelIndex].addAll(sampledData)
        }
        return result.map { it.toTypedArray() }.toTypedArray()
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
