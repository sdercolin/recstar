package audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import const.WavFormat
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
import kotlinx.io.writeIntLe
import kotlinx.io.writeShortLe
import repository.AppPreferenceRepository
import ui.common.UnexpectedErrorNotifier
import ui.model.AppContext
import util.Log
import util.runCatchingCancellable
import util.toJavaFile
import util.writeRawString

class AudioRecorderImpl(
    private val listener: AudioRecorder.Listener,
    context: AppContext,
    private val unexpectedErrorNotifier: UnexpectedErrorNotifier,
    private val appPreferenceRepository: AppPreferenceRepository,
) : AudioRecorder {
    private val coroutineScope = context.coroutineScope
    private var job: Job? = null
    private var cleanupJob: Job? = null
    private var audioRecord: AudioRecord? = null
    private val bufferSize = AudioRecord.getMinBufferSize(
        WavFormat.SAMPLE_RATE,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
    )
    private val rawData = Buffer()

    private fun createHeader(dataSize: Int): ByteArray {
        val buffer = Buffer()
        buffer.writeRawString(WavFormat.CHUNK_ID)
        buffer.writeIntLe(dataSize + WavFormat.HEADER_EXTRA_SIZE)
        buffer.writeRawString(WavFormat.FORMAT)

        buffer.writeRawString(WavFormat.SUBCHUNK_1_ID)
        buffer.writeIntLe(WavFormat.SUBCHUNK_1_SIZE)
        buffer.writeShortLe(WavFormat.AUDIO_FORMAT_PCM)
        buffer.writeShortLe(WavFormat.CHANNELS.toShort())
        buffer.writeIntLe(WavFormat.SAMPLE_RATE)
        buffer.writeIntLe(WavFormat.BYTE_RATE)
        val blockAlign = WavFormat.CHANNELS * WavFormat.BITS_PER_SAMPLE / 8
        buffer.writeShortLe(blockAlign.toShort())
        buffer.writeShortLe(WavFormat.BITS_PER_SAMPLE.toShort())

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
                cleanupJob?.join()
                cleanupJob = null
                Log.i("AudioRecorderImpl.start: path: ${output.absolutePath}")
                val audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    WavFormat.SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                )
                this@AudioRecorderImpl.audioRecord = audioRecord
                val audioBuffer = ShortArray(bufferSize)
                audioRecord.startRecording()
                withContext(Dispatchers.Main) {
                    listener.onStarted()
                }
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
                unexpectedErrorNotifier.notify(it)
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
                unexpectedErrorNotifier.notify(it)
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
                unexpectedErrorNotifier.notify(it)
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

    override val waveDataFlow: Flow<WavData> = _waveDataFlow
}

actual class AudioRecorderProvider actual constructor(
    private val listener: AudioRecorder.Listener,
    private val context: AppContext,
    private val unexpectedErrorNotifier: UnexpectedErrorNotifier,
    private val appPreferenceRepository: AppPreferenceRepository,
) {
    actual fun get(): AudioRecorder =
        AudioRecorderImpl(
            listener = listener,
            context = context,
            unexpectedErrorNotifier = unexpectedErrorNotifier,
            appPreferenceRepository = appPreferenceRepository,
        )
}
