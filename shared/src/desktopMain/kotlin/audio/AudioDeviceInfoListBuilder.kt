package audio

internal fun buildAudioDeviceInfoList(
    desiredDeviceName: String?,
    availableDeviceInfos: List<AudioDeviceInfo>,
): AudioDeviceInfoList? {
    if (availableDeviceInfos.isEmpty()) return null
    val deviceInfos = availableDeviceInfos.toMutableList()
    if (desiredDeviceName != null && deviceInfos.none { it.name == desiredDeviceName }) {
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
    return AudioDeviceInfoList(
        deviceInfos = deviceInfos,
        selectedDeviceInfo = deviceInfos.find { it.name == desiredDeviceName }
            ?: deviceInfos.find { it.isDefault }
            ?: deviceInfos.first(),
    )
}
