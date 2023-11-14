package ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import const.APP_NAME
import model.AppPreference
import repository.LocalAppPreferenceRepository
import ui.common.ScrollableColumn
import ui.model.Screen
import ui.string.*
import util.appVersion
import util.isDesktop
import util.runIf
import util.runIfHave

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
    val value by repository.state
    ScrollableColumn(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
        Group(title = string(Strings.PreferenceGroupAppearance)) {
            SelectionItem(
                title = string(Strings.PreferenceLanguage),
                value = value.language,
                onValueChanged = { repository.update { copy(language = it) } },
                options = AppPreference.Language.values().toList(),
            )
            SelectionItem(
                title = string(Strings.PreferenceTheme),
                value = value.theme,
                onValueChanged = { repository.update { copy(theme = it) } },
                options = AppPreference.Theme.values().toList().runIf(isDesktop) {
                    minus(AppPreference.Theme.System)
                },
            )
        }
        Group(title = string(Strings.PreferenceGroupMisc)) {
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
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp),
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
private fun Item(
    title: String,
    info: String,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .runIfHave(onClick) { clickable(onClick = it) }
            .padding(vertical = 16.dp, horizontal = 32.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.body1,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = info,
            style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)),
            maxLines = 1,
        )
    }
}