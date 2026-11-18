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
        currentDesktopAudioEngine().createRecorder(listener, errorNotifier, appPreferenceRepository)
}

actual class AudioPlayerProvider actual constructor(
    private val listener: AudioPlayer.Listener,
    private val context: AppContext,
    private val errorNotifier: ErrorNotifier,
    private val appPreferenceRepository: AppPreferenceRepository,
) {
    actual fun get(): AudioPlayer =
        currentDesktopAudioEngine().createPlayer(listener, context, errorNotifier, appPreferenceRepository)
}
