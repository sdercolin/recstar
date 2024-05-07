package ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.AlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import audio.model.AudioDeviceInfoList
import audio.model.getAudioFormat
import audio.model.getAudioInputDeviceInfos
import audio.model.getAudioOutputDeviceInfos
import audio.model.isSupported
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import const.APP_NAME
import io.LocalFileInteractor
import io.Paths
import model.AppPreference
import repository.LocalAppPreferenceRepository
import repository.LocalGuideAudioRepository
import repository.LocalReclistRepository
import repository.LocalSessionRepository
import ui.common.ActionMenu
import ui.common.ActionMenuItem
import ui.common.DisabledMutableInteractionSource
import ui.common.LocalAlertDialogController
import ui.common.ScrollableColumn
import ui.model.LocalSafeAreaInsets
import ui.model.Screen
import ui.string.*
import util.Log
import util.appVersion
import util.isDesktop
import util.isMacIntel
import util.runIf
import util.runIfHave
import util.useIosStyle

object PreferenceScreen : Screen {
    @Composable
    override fun getTitle(): String = string(Strings.PreferenceScreenTitle)

    @Composable
    override fun Actions() = ScreenActions()

    @Composable
    override fun Content() = ScreenContent()
}

@Composable
private fun ScreenActions() {
    ActionMenu { closeMenu ->
        val alertDialogController = LocalAlertDialogController.current
        val repository = LocalAppPreferenceRepository.current
        ActionMenuItem(
            text = string(Strings.MenuSettingsClearSettings),
            icon = Icons.Default.Refresh,
            onClick = {
                closeMenu()
                model.Actions.clearSettings(alertDialogController, repository)
            },
        )
        if (isDesktop) {
            ActionMenuItem(
                text = string(Strings.MenuSettingsClearAppData),
                icon = Icons.Default.RestartAlt,
                onClick = {
                    closeMenu()
                    model.Actions.clearAppData(alertDialogController)
                },
            )
        }
    }
}

@Composable
private fun ScreenContent() {
    val repository = LocalAppPreferenceRepository.current
    val navigator = LocalNavigator.currentOrThrow
    val fileInteractor = LocalFileInteractor.current
    val value by repository.flow.collectAsState()
    ScrollableColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background).padding(
            start = LocalSafeAreaInsets.current.leftDp(8f),
            end = LocalSafeAreaInsets.current.rightDp(8f),
        ),
    ) {
        Group(title = string(Strings.PreferenceGroupAppearance)) {
            SelectionItem(
                title = string(Strings.PreferenceLanguage),
                value = value.language,
                onValueChanged = { repository.update { copy(language = it) } },
                options = AppPreference.Language.entries.toList(),
            )
            SelectionItem(
                title = string(Strings.PreferenceTheme),
                value = value.theme,
                onValueChanged = { repository.update { copy(theme = it) } },
                options = AppPreference.Theme.entries.toList().runIf(isDesktop) {
                    minus(AppPreference.Theme.System)
                },
            )
            SelectionItem(
                title = string(Strings.PreferenceOrientation),
                value = value.orientation,
                onValueChanged = { repository.update { copy(orientation = it) } },
                options = AppPreference.ScreenOrientation.entries.toList(),
            )
        }
        Group(title = string(Strings.PreferenceGroupRecording)) {
            SwitchItem(
                title = string(Strings.PreferenceContinuousRecording),
                info = string(Strings.PreferenceContinuousRecordingDescription),
                value = value.recording.continuous,
                onValueChanged = { repository.update { copy(recording = recording.copy(continuous = it)) } },
            )
            SwitchItem(
                title = string(Strings.PreferenceTrimRecording),
                info = string(Strings.PreferenceTrimRecordingDescription),
                value = value.recording.trim,
                onValueChanged = { repository.update { copy(recording = recording.copy(trim = it)) } },
            )
            SwitchItem(
                title = string(Strings.PreferenceRecordWhileHolding),
                info = null,
                value = value.recording.recordWhileHolding,
                onValueChanged = { repository.update { copy(recording = recording.copy(recordWhileHolding = it)) } },
            )
            if (isDesktop) {
                SelectionItem(
                    title = string(Strings.PreferenceRecordingShortKey),
                    value = value.recording.recordingShortKey,
                    onValueChanged = { repository.update { copy(recording = recording.copy(recordingShortKey = it)) } },
                    options = AppPreference.RecordingShortKey.entries.toList(),
                )
            }
            SwitchItem(
                title = string(Strings.PreferenceAutoListenBack),
                info = string(Strings.PreferenceAutoListenBackDescription),
                value = value.recording.autoListenBack,
                onValueChanged = { repository.update { copy(recording = recording.copy(autoListenBack = it)) } },
            )
            SwitchItem(
                title = string(Strings.PreferenceAutoNext),
                info = string(Strings.PreferenceAutoNextDescription),
                value = value.recording.autoNext,
                onValueChanged = { repository.update { copy(recording = recording.copy(autoNext = it)) } },
            )
        }
        // Disable audio device selection on Intel Macs due to a bug in the audio library
        val enableDeviceSettings = isDesktop && !isMacIntel
        Group(
            title = string(Strings.PreferenceGroupAudio),
            description = string(Strings.PreferenceGroupAudioDescription).takeIf { enableDeviceSettings },
        ) {
            if (enableDeviceSettings) {
                val allInputDeviceInfo = produceState<AudioDeviceInfoList?>(
                    null,
                    value.desiredInputName,
                    value.sampleRate,
                    value.bitDepth,
                ) {
                    this.value = getAudioInputDeviceInfos(value.desiredInputName, value.getAudioFormat())
                }
                allInputDeviceInfo.value?.let { info ->
                    SelectionItem(
                        title = string(Strings.PreferenceInputDeviceName),
                        value = info.selectedDeviceInfo,
                        onValueChanged = { repository.update { copy(desiredInputName = it.name) } },
                        options = info.deviceInfos.filterNot { it.notFound },
                    )
                }
                val allOutputDeviceInfo = produceState<AudioDeviceInfoList?>(
                    null,
                    value.desiredOutputName,
                    value.sampleRate,
                    value.bitDepth,
                ) {
                    this.value = getAudioOutputDeviceInfos(value.desiredOutputName, value.getAudioFormat())
                }
                allOutputDeviceInfo.value?.let { info ->
                    SelectionItem(
                        title = string(Strings.PreferenceOutputDeviceName),
                        value = info.selectedDeviceInfo,
                        onValueChanged = { repository.update { copy(desiredOutputName = it.name) } },
                        options = info.deviceInfos.filterNot { it.notFound },
                    )
                }
            }
            SelectionItem(
                title = string(Strings.PreferenceSampleRate),
                value = value.sampleRate,
                onValueChanged = { repository.update { copy(sampleRate = it) } },
                options = AppPreference.SampleRateOption.entries.toList(),
            )
            val bitDepthOptions: State<List<AppPreference.BitDepthOption>> = produceState(listOf()) {
                this.value = AppPreference.BitDepthOption.entries.filter {
                    it.isSupported(value)
                }
            }
            if (bitDepthOptions.value.isNotEmpty()) {
                SelectionItem(
                    title = string(Strings.PreferenceBitDepth),
                    value = value.bitDepth,
                    onValueChanged = { repository.update { copy(bitDepth = it) } },
                    options = bitDepthOptions.value.toList(),
                )
            }
        }
        Group(title = string(Strings.PreferenceGroupView)) {
            SelectionItem(
                title = string(Strings.PreferenceTitleBarStyle),
                value = value.titleBarStyle,
                onValueChanged = { repository.update { copy(titleBarStyle = it) } },
                options = AppPreference.TitleBarStyle.entries.toList(),
            )
        }
        Group(title = string(Strings.PreferenceGroupMisc)) {
            SwitchItem(
                title = string(Strings.PreferenceAlwaysConfirmTextEncoding),
                info = null,
                value = value.alwaysConfirmTextEncoding,
                onValueChanged = { repository.update { copy(alwaysConfirmTextEncoding = it) } },
            )
            if (isDesktop) {
                val guideAudioRepository = LocalGuideAudioRepository.current
                val sessionRepository = LocalSessionRepository.current
                val reclistRepository = LocalReclistRepository.current
                key(value.customContentRootPath) {
                    Item(
                        title = string(Strings.PreferenceContentRootLocation),
                        info = Paths.contentRoot.parentFile?.absolutePath ?: "",
                        onClick = {
                            fileInteractor.pickFile(
                                title = stringStatic(Strings.PreferenceContentRootLocation),
                                allowedExtensions = listOf(""),
                                initialDirectory = Paths.contentRoot.parentFile,
                                onFinish = { file ->
                                    file ?: return@pickFile
                                    Log.i("Setting custom content root: ${file.absolutePath}")
                                    repository.update { copy(customContentRootPath = file.absolutePath) }
                                    Paths.moveContentRoot(file)
                                    guideAudioRepository.init()
                                    sessionRepository.init()
                                    reclistRepository.init()
                                },
                            )
                        },
                    )
                }
            }
            Item(
                title = string(Strings.PreferenceAbout),
                info = "$APP_NAME $appVersion",
                onClick = { navigator push AboutScreen },
            )
        }
    }
}

@Composable
private fun Group(
    title: String,
    description: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        Text(
            modifier = Modifier.padding(
                start = 40.dp,
                end = 40.dp,
                top = 8.dp,
                bottom = if (description != null) 8.dp else 16.dp,
            ),
            text = title,
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.primary,
            maxLines = 1,
        )
        description?.let {
            Text(
                modifier = Modifier.padding(start = 40.dp, end = 80.dp, bottom = 16.dp),
                text = it,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                maxLines = 8,
                overflow = TextOverflow.Ellipsis,
            )
        }
        content()
        Divider(modifier = Modifier.padding(start = 24.dp))
    }
}

@Composable
private fun <T : LocalizedText> SelectionItem(
    title: String,
    value: T?,
    onValueChanged: (T) -> Unit,
    options: List<T>,
    isError: (T) -> Boolean = { false },
) {
    var isShowingDialog by remember { mutableStateOf(false) }
    Item(
        title = title,
        info = value?.getText() ?: "",
        isError = value?.let(isError) ?: false,
        onClick = {
            if (options.isNotEmpty()) {
                isShowingDialog = true
            }
        },
    )
    if (isShowingDialog) {
        AlertDialog(
            onDismissRequest = { isShowingDialog = false },
            title = {
                Text(text = title, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("", modifier = Modifier.height(16.dp))
                    options.forEach { option ->
                        Text(
                            text = option.getText(),
                            modifier = Modifier.fillMaxWidth()
                                .clickable {
                                    onValueChanged(option)
                                    isShowingDialog = false
                                }
                                .padding(vertical = 12.dp),
                            color = MaterialTheme.colors.onSurface,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isShowingDialog = false }) {
                    Text(text = string(Strings.CommonCancel))
                }
            },
            properties = DialogProperties(dismissOnClickOutside = true),
        )
    }
}

@Composable
private fun SwitchItem(
    title: String,
    info: String?,
    value: Boolean,
    onValueChanged: (Boolean) -> Unit,
) {
    Item(
        title = title,
        info = info,
        subItem = {
            Switch(
                modifier = Modifier.size(32.dp),
                checked = value,
                onCheckedChange = null,
                colors = MaterialTheme.colors.run {
                    SwitchDefaults.colors(
                        checkedThumbColor = primary,
                        checkedTrackColor = primary,
                    )
                },
                // disable interaction
                interactionSource = remember { DisabledMutableInteractionSource() },
            )
        },
        onClick = { onValueChanged(!value) },
    )
}

@Composable
private fun Item(
    title: String,
    info: String? = null,
    isError: Boolean = false,
    subItem: @Composable (() -> Unit)? = if (useIosStyle) {
        {
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
            )
        }
    } else {
        null
    },
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(start = 24.dp)
            .runIfHave(onClick) { clickable(onClick = it) }
            .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = if (isDesktop) 40.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.body1,
                maxLines = 1,
            )
            info?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = info,
                    style = MaterialTheme.typography.caption.copy(
                        color = if (isError) {
                            MaterialTheme.colors.error
                        } else {
                            MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        },
                    ),
                    maxLines = 1,
                )
            }
        }
        subItem?.let {
            it()
        }
    }
}
