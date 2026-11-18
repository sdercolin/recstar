package audio

import io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import repository.AppPreferenceRepository

/**
 * An [AudioRecorder] that delegates to the engine selected in the preferences. The engine is switched only when
 * a new recording starts, so a running recording is never interrupted.
 */
internal class EngineSwitchingRecorder(
    private val appPreferenceRepository: AppPreferenceRepository,
    private val factory: (DesktopAudioEngine) -> AudioRecorder,
) : AudioRecorder {
    private var key = appPreferenceRepository.value.desktopAudioEngineKey()
    private val delegate = MutableStateFlow(factory(key.resolve()))

    override fun start(output: File) {
        val newKey = appPreferenceRepository.value.desktopAudioEngineKey()
        if (newKey != key && !delegate.value.isRecording()) {
            delegate.value.dispose()
            key = newKey
            delegate.value = factory(newKey.resolve())
        }
        delegate.value.start(output)
    }

    override fun stop() = delegate.value.stop()

    override fun isRecording(): Boolean = delegate.value.isRecording()

    override fun dispose() = delegate.value.dispose()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val waveDataFlow: Flow<WavData> = delegate.flatMapLatest { it.waveDataFlow }
}
