package audio.javasound

import audio.AudioDeviceInfo
import audio.AudioDeviceInfoList
import audio.AudioFormat
import audio.buildAudioDeviceInfoList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.Line
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.TargetDataLine

internal suspend fun getJavaSoundInputDeviceInfos(
    desiredDeviceName: String?,
    audioFormat: AudioFormat,
): AudioDeviceInfoList? = getJavaSoundDeviceInfos(desiredDeviceName, audioFormat, TargetDataLine::class.java)

internal suspend fun getJavaSoundOutputDeviceInfos(
    desiredDeviceName: String?,
    audioFormat: AudioFormat,
): AudioDeviceInfoList? = getJavaSoundDeviceInfos(desiredDeviceName, audioFormat, SourceDataLine::class.java)

private suspend fun getJavaSoundDeviceInfos(
    desiredDeviceName: String?,
    audioFormat: AudioFormat,
    lineClass: Class<out Line>,
): AudioDeviceInfoList? =
    withContext(Dispatchers.IO) {
        val mixerInfos = AudioSystem.getMixerInfo()
        val defaultMixerInfo = AudioSystem.getMixer(null).mixerInfo
        val mappedFormat = audioFormat.toJavaAudioFormat()

        val deviceInfos = mixerInfos
            .filter {
                AudioSystem.getMixer(it)
                    .isLineSupported(DataLine.Info(lineClass, mappedFormat))
            }
            .map { info ->
                AudioDeviceInfo(
                    name = info.name,
                    displayName = info.name,
                    isDefault = info.name == defaultMixerInfo.name,
                )
            }

        buildAudioDeviceInfoList(desiredDeviceName, deviceInfos)
    }
