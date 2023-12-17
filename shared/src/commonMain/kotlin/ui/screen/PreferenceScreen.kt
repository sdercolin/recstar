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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
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
import ui.common.DisabledMutableInteractionSource
import ui.common.ScrollableColumn
import ui.model.Screen
import ui.string.*
import util.Log
import util.appVersion
import util.isDesktop
import util.runIf
import util.runIfHave
import util.useIosStyle

object PreferenceScreen : Screen {
    @Composable
    override fun getTitle(): String = string(Strings.PreferenceScreenTitle)

    @Composable
    override fun Content() = ScreenContent()
}

@Composable
private fun ScreenContent() {
    val repository = LocalAppPreferenceRepository.current
    val navigator = LocalNavigator.currentOrThrow
    val fileInteractor = LocalFileInteractor.current
    val value by repository.flow.collectAsState()
    ScrollableColumn(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
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
        }
        Group(title = string(Strings.PreferenceGroupReclist)) {
            SwitchItem(
                title = string(Strings.PreferenceKanaNormalization),
                value = value.normalizeKanaNfc,
                onValueChanged = { repository.update { copy(normalizeKanaNfc = it) } },
            )
        }
        Group(title = string(Strings.PreferenceGroupMisc)) {
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
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        Text(
            modifier = Modifier.padding(start = 40.dp, end = 40.dp, top = 8.dp, bottom = 16.dp),
            text = title,
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.primary,
            maxLines = 1,
        )
        content()
        Divider(modifier = Modifier.padding(start = 24.dp))
    }
}

@Composable
private fun <T : LocalizedTest> SelectionItem(
    title: String,
    value: T,
    onValueChanged: (T) -> Unit,
    options: List<T>,
) {
    var isShowingDialog by remember { mutableStateOf(false) }
    Item(
        title = title,
        info = value.getText(),
        onClick = { isShowingDialog = true },
    )
    if (isShowingDialog) {
        AlertDialog(
            onDismissRequest = { isShowingDialog = false },
            title = {
                Text(text = title)
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
                            style = MaterialTheme.typography.body1,
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
    value: Boolean,
    onValueChanged: (Boolean) -> Unit,
) {
    Item(
        title = title,
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
            .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 40.dp),
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
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
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
