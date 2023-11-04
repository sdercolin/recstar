package graph

import io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class WaveformPainter(
    private val recordingFlow: Flow<FloatArray>,
    private val coroutineScope: CoroutineScope,
) {
    private var pointPerPixel = 60

    private val _flow = MutableStateFlow(emptyArray<FloatArray>())
    val flow: Flow<Array<FloatArray>> = _flow

    fun switch(file: File) {
        // TODO: load file and draw
        _flow.value = emptyArray()
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

    fun onStopRecording() {
        job?.cancel()
        job = null
    }
}
