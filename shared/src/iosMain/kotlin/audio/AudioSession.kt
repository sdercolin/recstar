package audio

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionAllowBluetooth
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeDefault
import platform.AVFAudio.setActive
import util.withNSError

@OptIn(ExperimentalForeignApi::class)
object AudioSession {
    fun initialize() {
        withNSError { e ->
            AVAudioSession.sharedInstance().setCategory(
                AVAudioSessionCategoryPlayAndRecord,
                mode = AVAudioSessionModeDefault,
                options = AVAudioSessionCategoryOptionAllowBluetooth,
                error = e,
            )
        }
        withNSError { e ->
            AVAudioSession.sharedInstance().setActive(true, error = e)
        }
    }
}
