package audio.javasound

import androidx.compose.runtime.Stable
import audio.AudioDeviceInfoList
import audio.AudioRecorder
import audio.WavData
import audio.getAudioFormat
import exception.UnsupportedAudioDeviceException
import exception.UnsupportedAudioFormatException
import io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import repository.AppPreferenceRepository
import ui.common.ErrorNotifier
import util.Log
import util.runCatchingCancellable
import util.toJavaFile
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.Mixer
import javax.sound.sampled.TargetDataLine

@Stable
internal class JavaSoundRecorder(
    private val listener: AudioRecorder.Listener,
    private val errorNotifier: ErrorNotifier,
    private val appPreferenceRepository: AppPreferenceRepository,
) : AudioRecorder {
    private var line: TargetDataLine? = null
    private var job: Job? = null
    private var cleanupJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var stream: InterceptedAudioInputStream? = null

    override fun start(output: File) {
        if (job?.isActive == true) {
            Log.w("JavaSoundRecorder.start: already started")
            return
        }
        job = scope.launch {
            runCatchingCancellable {
                cleanupJob?.join()
                cleanupJob = null
                _waveDataFlow.value = arrayOf(FloatArray(0))
                val line = getTargetLine().apply {
                    open(format)
                    start()
                }
                this@JavaSoundRecorder.line = line
                Log.i("JavaSoundRecorder.start: path: ${output.absolutePath}, format: ${line.format}")
                listener.onStarted()
                withContext(Dispatchers.IO) {
                    val stream = InterceptedAudioInputStream(line, 1792, _waveDataFlow)
                    this@JavaSoundRecorder.stream = stream
                    AudioSystem.write(
                        AudioInputStream(
                            // stream =
                            stream,
                            // format =
                            appPreferenceRepository.value.getAudioFormat().toJavaAudioFormat(),
                            // length =
                            AudioSystem.NOT_SPECIFIED.toLong(),
                        ),
                        AudioFileFormat.Type.WAVE,
                        output.toJavaFile(),
                    )
                }
            }.onFailure {
                errorNotifier.notify(it)
                dispose()
            }
        }
    }

    private suspend fun getTargetLine(): TargetDataLine {
        val mixerInfos = AudioSystem.getMixerInfo()
        val format = appPreferenceRepository.value.getAudioFormat()
        val dataLineInfo = DataLine.Info(TargetDataLine::class.java, format.toJavaAudioFormat())
        val deviceInfos = getJavaSoundInputDeviceInfos(appPreferenceRepository.value.desiredInputName, format)
            ?: throw UnsupportedAudioFormatException(format)
        if (!AudioSystem.isLineSupported(dataLineInfo)) {
            throw UnsupportedAudioFormatException(format)
        }
        val selectedMixerInfo = selectMixer(mixerInfos, deviceInfos)
        val selectedMixer = AudioSystem.getMixer(selectedMixerInfo)
        return try {
            selectedMixer.getLine(dataLineInfo) as TargetDataLine
        } catch (e: Exception) {
            throw UnsupportedAudioDeviceException()
        }
    }

    private fun selectMixer(
        mixerInfos: Array<out Mixer.Info>,
        deviceInfos: AudioDeviceInfoList,
    ): Mixer.Info {
        mixerInfos.find { it.name == deviceInfos.selectedDeviceInfo.name }?.let {
            return it
        }
        val default = deviceInfos.deviceInfos.firstOrNull { it.isDefault } ?: deviceInfos.deviceInfos.first()
        return mixerInfos.find { it.name == default.name } ?: mixerInfos.first()
    }

    override fun stop() {
        cleanupJob = scope.launch(Dispatchers.IO) {
            runCatchingCancellable {
                line?.stop()
                line?.flush()
                line?.close()
                stream?.close()
                stream = null
                job?.cancelAndJoin()
                line = null
                Log.i("JavaSoundRecorder.stop: stopped")
                withContext(Dispatchers.Main) {
                    listener.onStopped()
                }
            }.onFailure {
                errorNotifier.notify(it)
                dispose()
            }
        }
    }

    override fun isRecording(): Boolean = line?.isActive == true

    override fun dispose() {
        runCatching {
            cleanupJob?.cancel()
            job?.cancel()
            line?.stop()
            line?.flush()
            line?.close()
            line = null
        }.onFailure {
            errorNotifier.notify(it)
        }
    }

    private val _waveDataFlow = MutableStateFlow(arrayOf(FloatArray(0)))
    override val waveDataFlow: Flow<WavData> = _waveDataFlow
}
