package graph

import audio.WavReader
import audio.model.FundamentalConfigs
import audio.model.WavData
import io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ui.common.ErrorNotifier

class GraphPainter(
    private val recordingFlow: Flow<WavData>,
    private val coroutineScope: CoroutineScope,
    private val errorNotifier: ErrorNotifier,
) {
    private var pointPerPixel = 60

    // Layers: [channel][point][max/min], value range: [-1, 1]
    private val _waveform = MutableStateFlow(emptyArray<Array<FloatArray>>())
    val waveform: Flow<Array<Array<FloatArray>>> = _waveform

    private val pitchPainter = PitchPainter(pointPerPixel)

    val pitch: Flow<PitchPainter.PitchGraphData> get() = pitchPainter.pitch

    private val wavReader = WavReader()

    private var pendingFile: File? = null

    fun switch(file: File) {
        pendingFile = file
        if (recordingFlowCollectionJob?.isActive == true) return
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
                _waveform.value = getSampledData(it.data)
                pendingFilePitchCalculationJob?.cancel()
                pendingFilePitchCalculationJob = coroutineScope.launch {
                    pitchPainter.putData(it.data, it.sampleRate, FundamentalConfigs())
                }
            }
        } else {
            _waveform.value = emptyArray()
        }
    }

    private var recordingFlowCollectionJob: Job? = null
    private var pendingFilePitchCalculationJob: Job? = null

    fun onStartRecording() {
        recordingFlowCollectionJob?.cancel()
        recordingFlowCollectionJob = coroutineScope.launch(Dispatchers.Default) {
            recordingFlow.collect { data ->
                _waveform.value = getSampledData(data)
            }
        }
    }

    fun clear() {
        recordingFlowCollectionJob?.cancel()
        recordingFlowCollectionJob = null
        pendingFilePitchCalculationJob?.cancel()
        pendingFilePitchCalculationJob = null
        _waveform.value = emptyArray()
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
            recordingFlowCollectionJob?.cancelAndJoin()
            recordingFlowCollectionJob = null
            if (!isSwitchingScheduled) {
                consumePendingFile()
            }
        }
    }

    fun dispose() {
        recordingFlowCollectionJob?.cancel()
        recordingFlowCollectionJob = null
        pendingFile = null
    }
}
