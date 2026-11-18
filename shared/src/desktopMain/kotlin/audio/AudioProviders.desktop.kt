package audio

import repository.AppPreferenceRepository
import ui.common.ErrorNotifier
import ui.model.AppContext

actual class AudioRecorderProvider actual constructor(
    private val listener: AudioRecorder.Listener,
    private val context: AppContext,
    private val errorNotifier: ErrorNotifier,
    private val appPreferenceRepository: AppPreferenceRepository,
) {
    actual fun get(): AudioRecorder =
        EngineSwitchingRecorder(appPreferenceRepository) { engine ->
            engine.createRecorder(listener, errorNotifier, appPreferenceRepository)
        }
}

actual class AudioPlayerProvider actual constructor(
    private val listener: AudioPlayer.Listener,
    private val context: AppContext,
    private val errorNotifier: ErrorNotifier,
    private val appPreferenceRepository: AppPreferenceRepository,
) {
    actual fun get(): AudioPlayer =
        EngineSwitchingPlayer(appPreferenceRepository) { engine ->
            engine.createPlayer(listener, context, errorNotifier, appPreferenceRepository)
        }
}
