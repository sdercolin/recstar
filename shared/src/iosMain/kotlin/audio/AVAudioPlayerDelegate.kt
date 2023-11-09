package audio

import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioPlayerDelegateProtocol
import platform.Foundation.NSError
import platform.darwin.NSObject
import util.Log

class AVAudioPlayerDelegate(private val onFinishedPlaying: () -> Unit) : NSObject(), AVAudioPlayerDelegateProtocol {
    override fun audioPlayerDidFinishPlaying(
        player: AVAudioPlayer,
        successfully: Boolean,
    ) {
        if (!successfully) {
            Log.w("AudioPlayerDelegate.audioPlayerDidFinishPlaying: not successfully")
        }
        onFinishedPlaying()
    }

    override fun audioPlayerDecodeErrorDidOccur(
        player: AVAudioPlayer,
        error: NSError?,
    ) {
        Log.e("AudioPlayerDelegate.audioPlayerDecodeErrorDidOccur: ${error?.localizedDescription}")
    }

    override fun audioPlayerBeginInterruption(player: AVAudioPlayer) {
        Log.w("AudioPlayerDelegate.audioPlayerBeginInterruption")
    }

    override fun audioPlayerEndInterruption(player: AVAudioPlayer) {
        Log.w("AudioPlayerDelegate.audioPlayerEndInterruption")
    }
}
