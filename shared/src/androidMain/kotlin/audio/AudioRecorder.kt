package audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import const.WavFormat
import exception.UnsupportedAudioFormatException
import io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.io.writeFloatLe
import kotlinx.io.writeIntLe
import kotlinx.io.writeShortLe
import repository.AppPreferenceRepository
import ui.common.ErrorNotifier
import ui.model.AppContext
import util.Log
import util.runCatchingCancellable
import util.toJavaFile
import util.writeRawString

class AudioRecorderImpl(
    private val listener: AudioRecorder.Listener,
    context: AppContext,
    private val errorNotifier: ErrorNotifier,
    private val appPreferenceRepository: AppPreferenceRepository,
) : AudioRecorder {
    private val coroutineScope = context.coroutineScope
    private var job: Job? = null
    private var cleanupJob: Job? = null
    private var audioRecord: AudioRecord? = null
    private val format get() = appPreferenceRepository.value.getAudioFormat()
    private val bufferSize
        get() = AudioRecord.getMinBufferSize(
            format.sampleRate,
            format.channelCount,
            when (format.bitDepth) {
                16 -> AudioFormat.ENCODING_PCM_16BIT
                32 -> when (format.floating) {
                    true -> AudioFormat.ENCODING_PCM_FLOAT
                    false -> throw UnsupportedAudioFormatException(format)
                }
                else -> throw UnsupportedAudioFormatException(format)
            },
        )
    private val rawData = Buffer()

    private fun createHeader(dataSize: Int): ByteArray {
        val buffer = Buffer()
        val format = format
        buffer.writeRawString(WavFormat.CHUNK_ID)
        buffer.writeIntLe(dataSize + WavFormat.HEADER_EXTRA_SIZE)
        buffer.writeRawString(WavFormat.FORMAT)

        buffer.writeRawString(WavFormat.SUBCHUNK_1_ID)
        buffer.writeIntLe(WavFormat.SUBCHUNK_1_SIZE)
        val audioFormat = if (format.floating) {
            WavFormat.AUDIO_FORMAT_FLOAT
        } else {
            WavFormat.AUDIO_FORMAT_PCM
        }
        buffer.writeShortLe(audioFormat)
        buffer.writeShortLe(format.channelCount.toShort())
        buffer.writeIntLe(format.sampleRate)
        val byteRate = format.sampleRate * format.channelCount * format.bitDepth / 8
        buffer.writeIntLe(byteRate)
        val blockAlign = format.channelCount * format.bitDepth / 8
        buffer.writeShortLe(blockAlign.toShort())
        buffer.writeShortLe(format.bitDepth.toShort())

        buffer.writeRawString(WavFormat.SUBCHUNK_2_ID)
        buffer.writeIntLe(dataSize)
        return buffer.readByteArray()
    }

    @SuppressLint("MissingPermission")
    override fun start(output: File) {
        if (job?.isActive == true) {
            Log.w("AudioRecorderImpl.start: already started")
            return
        }
        waveData.clear()
        _waveDataFlow.value = arrayOf(FloatArray(0))
        rawData.clear()
        job = coroutineScope.launch(Dispatchers.IO) {
            runCatchingCancellable {
                val format = format
                cleanupJob?.join()
                cleanupJob = null
                Log.i("AudioRecorderImpl.start: path: ${output.absolutePath}, format: $format")
                val audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    format.sampleRate,
                    if (format.channelCount == 1) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_IN_STEREO,
                    when (format.bitDepth) {
                        16 -> AudioFormat.ENCODING_PCM_16BIT
                        32 -> when (format.floating) {
                            true -> AudioFormat.ENCODING_PCM_FLOAT
                            false -> throw UnsupportedAudioFormatException(format)
                        }
                        else -> throw UnsupportedAudioFormatException(format)
                    },
                    bufferSize,
                )
                this@AudioRecorderImpl.audioRecord = audioRecord
                audioRecord.startRecording()
                withContext(Dispatchers.Main) {
                    listener.onStarted()
                }
                if (format.bitDepth == 16) {
                    val audioBuffer = ShortArray(bufferSize)
                    while (isActive && this@AudioRecorderImpl.isRecording()) {
                        val readResult = audioRecord.read(audioBuffer, 0, audioBuffer.size)
                        if (readResult > 0) {
                            val validAudioData = audioBuffer.copyOfRange(0, readResult)
                            for (i in validAudioData.indices) {
                                rawData.writeShortLe(validAudioData[i])
                            }
                            addWaveData(validAudioData)
                        } else {
                            Log.w("Error reading audio data: $readResult")
                        }
                    }
                } else {
                    val audioBuffer = FloatArray(bufferSize)
                    while (isActive && this@AudioRecorderImpl.isRecording()) {
                        val readResult = audioRecord.read(
                            audioBuffer,
                            0,
                            audioBuffer.size,
                            AudioRecord.READ_BLOCKING,
                        )
                        if (readResult > 0) {
                            val validAudioData = audioBuffer.copyOfRange(0, readResult)
                            for (i in validAudioData.indices) {
                                rawData.writeFloatLe(validAudioData[i])
                            }
                            addWaveData(validAudioData)
                        } else {
                            Log.w("Error reading audio data: $readResult")
                        }
                    }
                }
                audioRecord.stop()
                audioRecord.release()
                output.toJavaFile().outputStream().use { stream ->
                    stream.write(createHeader(rawData.size.toInt()))
                    stream.write(rawData.readByteArray())
                    stream.flush()
                }
                while (output.exists().not()) {
                    Log.d("AudioRecorderImpl.start: waiting for file to be created")
                    Thread.sleep(100)
                }
            }.onFailure {
                errorNotifier.notify(it)
                dispose()
            }
        }
    }

    override fun stop() {
        cleanupJob = coroutineScope.launch(Dispatchers.IO) {
            runCatchingCancellable {
                job?.cancelAndJoin()
                audioRecord?.release()
                audioRecord = null
                waveData.clear()
                rawData.clear()
                Log.i("AudioRecorderImpl.stop: stopped")
                withContext(Dispatchers.Main) {
                    listener.onStopped()
                }
            }.onFailure {
                errorNotifier.notify(it)
                dispose()
            }
        }
    }

    override fun isRecording(): Boolean = audioRecord != null

    override fun dispose() {
        coroutineScope.launch {
            runCatchingCancellable {
                job?.cancelAndJoin()
                cleanupJob?.cancelAndJoin()
                audioRecord?.release()
                job = null
                audioRecord = null
                cleanupJob = null
            }.onFailure {
                errorNotifier.notify(it)
            }
        }
    }

    private val waveData = mutableListOf<Float>()
    private val _waveDataFlow = MutableStateFlow(arrayOf(FloatArray(0)))

    private fun addWaveData(buffer: ShortArray) {
        val value = buffer.map { it.toFloat() / Short.MAX_VALUE }
        waveData.addAll(value)
        _waveDataFlow.value = waveData.toFloatArray().map { arrayOf(it).toFloatArray() }.toTypedArray()
    }

    private fun addWaveData(buffer: FloatArray) {
        waveData.addAll(buffer.toList())
        _waveDataFlow.value = waveData.toFloatArray().map { arrayOf(it).toFloatArray() }.toTypedArray()
    }

    override val waveDataFlow: Flow<WavData> = _waveDataFlow
}

actual class AudioRecorderProvider actual constructor(
    private val listener: AudioRecorder.Listener,
    private val context: AppContext,
    private val errorNotifier: ErrorNotifier,
    private val appPreferenceRepository: AppPreferenceRepository,
) {
    actual fun get(): AudioRecorder =
        AudioRecorderImpl(
            listener = listener,
            context = context,
            errorNotifier = errorNotifier,
            appPreferenceRepository = appPreferenceRepository,
        )
}
