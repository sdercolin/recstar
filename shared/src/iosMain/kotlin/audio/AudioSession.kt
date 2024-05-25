package audio

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionAllowBluetooth
import platform.AVFAudio.AVAudioSessionCategoryOptionAllowBluetoothA2DP
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeDefault
import platform.AVFAudio.AVAudioSessionPortBuiltInMic
import platform.AVFAudio.AVAudioSessionPortDescription
import platform.AVFAudio.availableInputs
import platform.AVFAudio.setActive
import repository.AppPreferenceRepository
import util.Log
import util.withNSError

@OptIn(ExperimentalForeignApi::class)
object AudioSession {
    fun initialize(appPreferenceRepository: AppPreferenceRepository) {
        val preferBuiltInMic = appPreferenceRepository.value.preferBuiltInMic
        withNSError { e ->
            AVAudioSession.sharedInstance().apply {
                setCategory(
                    AVAudioSessionCategoryPlayAndRecord,
                    mode = AVAudioSessionModeDefault,
                    options = if (preferBuiltInMic) {
                        AVAudioSessionCategoryOptionAllowBluetoothA2DP
                    } else {
                        AVAudioSessionCategoryOptionAllowBluetooth
                    },
                    error = e,
                )
                val descs = availableInputs.orEmpty().filterIsInstance<AVAudioSessionPortDescription>()
                val inputDesc = if (preferBuiltInMic) {
                    descs.firstOrNull { it.portType == AVAudioSessionPortBuiltInMic }
                } else {
                    descs.firstOrNull { it.portType != AVAudioSessionPortBuiltInMic }
                }
                Log.i("AudioSession.initialize: inputDesc: $inputDesc")
                setPreferredInput(inputDesc, error = e)
            }
        }
        withNSError { e ->
            AVAudioSession.sharedInstance().setActive(true, error = e)
        }
    }
}
