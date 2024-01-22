package audio

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.TargetDataLine

actual suspend fun getAudioInputDeviceInfos(
    desiredDeviceName: String?,
    audioFormat: AudioFormat,
): AudioDeviceInfoList = getAudioDeviceInfos(desiredDeviceName, audioFormat, AudioDeviceType.INPUT)

actual suspend fun getAudioOutputDeviceInfos(
    desiredDeviceName: String?,
    audioFormat: AudioFormat,
): AudioDeviceInfoList = getAudioDeviceInfos(desiredDeviceName, audioFormat, AudioDeviceType.OUTPUT)

private enum class AudioDeviceType {
    INPUT,
    OUTPUT,
}

private suspend fun getAudioDeviceInfos(
    desiredDeviceName: String?,
    audioFormat: AudioFormat,
    deviceType: AudioDeviceType,
): AudioDeviceInfoList =
    withContext(Dispatchers.IO) {
        val mixerInfos = AudioSystem.getMixerInfo()
        val defaultMixerInfo = AudioSystem.getMixer(null).mixerInfo
        val mappedFormat = audioFormat.toJavaAudioFormat()

        val lineClass = when (deviceType) {
            AudioDeviceType.INPUT -> TargetDataLine::class.java
            AudioDeviceType.OUTPUT -> SourceDataLine::class.java
        }

        val deviceInfos = mixerInfos
            .filter {
                AudioSystem.getMixer(it)
                    .isLineSupported(DataLine.Info(lineClass, mappedFormat))
            }
            .map { info ->
                AudioDeviceInfo(
                    name = info.name,
                    displayName = info.description,
                    isDefault = info.name == defaultMixerInfo.name,
                )
            }.toMutableList()

        addDesiredDeviceIfNotFound(desiredDeviceName, deviceInfos)

        AudioDeviceInfoList(
            deviceInfos = deviceInfos,
            selectedDeviceInfo = deviceInfos.find { it.name == desiredDeviceName }
                ?: deviceInfos.find { it.isDefault }
                ?: deviceInfos.first(),
        )
    }

private fun addDesiredDeviceIfNotFound(
    desiredDeviceName: String?,
    deviceInfos: MutableList<AudioDeviceInfo>,
) {
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
}
