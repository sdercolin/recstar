package audio

import kotlinx.coroutines.Dispatchers
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine

actual suspend fun getAudioInputDeviceInfos(
    desiredDeviceName: String?,
    audioFormat: AudioFormat,
): AudioDeviceInfoList =
    with(Dispatchers.IO) {
        val mixerInfos = AudioSystem.getMixerInfo()
        val defaultMixerInfo = AudioSystem.getMixer(null).mixerInfo
        val mappedFormat = audioFormat.toJavaAudioFormat()
        val deviceInfos = mixerInfos
            .filter {
                AudioSystem.getMixer(it)
                    .isLineSupported(DataLine.Info(TargetDataLine::class.java, mappedFormat))
            }
            .map { info ->
                AudioDeviceInfo(
                    name = info.name,
                    displayName = info.description,
                    isDefault = info.name == defaultMixerInfo.name,
                )
            }.toMutableList()
        if (desiredDeviceName != null) {
            val desiredDeviceInfo = deviceInfos.find { it.name == desiredDeviceName }
            if (desiredDeviceInfo == null) {
                deviceInfos.add(
                    0,
                    AudioDeviceInfo(
                        name = desiredDeviceName,
                        displayName = desiredDeviceName,
                        isDefault = false,
                        notFound = true,
                    ),
                )
            }
        }
        AudioDeviceInfoList(
            deviceInfos = deviceInfos,
            selectedDeviceInfo = deviceInfos.find { it.name == desiredDeviceName }
                ?: deviceInfos.find { it.isDefault }
                ?: deviceInfos.first(),
        )
    }
