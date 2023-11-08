package audio

import io.File
import ui.model.AppContext

class AudioPlayerImpl : AudioPlayer {
    override fun play(file: File) {
        // TODO
    }

    override fun stop() {
        // TODO
    }

    override fun isPlaying(): Boolean {
        // TODO
        return false
    }

    override fun dispose() {
        // TODO
    }
}

actual class AudioPlayerProvider actual constructor(
    listener: AudioPlayer.Listener,
    context: AppContext,
) {
    actual fun get(): AudioPlayer = AudioPlayerImpl()
}
