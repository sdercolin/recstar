package ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import const.APP_NAME
import kotlinx.coroutines.flow.collectLatest
import model.Action
import repository.LocalAppActionStore
import ui.common.ActionMenu
import ui.common.ActionMenuItem
import ui.common.FloatingActionButtonWrapper
import ui.common.ScrollableLazyColumn
import ui.common.SearchBar
import ui.common.SortingButton
import ui.model.LocalSafeAreaInsets
import ui.model.Screen
import ui.screen.demo.DemoShowcaseScreen
import ui.string.*
import util.isDebug

object MainScreen : Screen {
    @Composable
    override fun getTitle(): String = APP_NAME

    @Composable
    override fun Actions() = ScreenAction()

    @Composable
    override fun Content() = ScreenContent()
}

@Composable
private fun MainScreen.ScreenAction() {
    val navigator = LocalNavigator.currentOrThrow
    val model = rememberMainScreenModel()
    val appActionStore = LocalAppActionStore.current
    LaunchedEffect(appActionStore, model) {
        appActionStore.actions.collectLatest {
            when (it) {
                Action.NewSession -> navigator push CreateSessionReclistScreen
                Action.EditList -> model.startSelectingForDeletion()
                else -> Unit
            }
        }
    }
    model.ActionButtonWrapper {
        ActionMenu { closeMenu ->
            ActionMenuItem(
                text = string(Strings.CommonEdit),
                icon = Icons.Default.Edit,
                onClick = {
                    closeMenu()
                    model.startSelectingForDeletion()
                },
            )
            ActionMenuItem(
                text = string(Strings.PreferenceScreenTitle),
                icon = Icons.Default.Settings,
                onClick = { navigator push PreferenceScreen },
            )
            if (isDebug) {
                ActionMenuItem(
                    text = "Dev Tools",
                    icon = Icons.Default.Code,
                    onClick = { navigator push DemoShowcaseScreen },
                )
            }
        }
    }
}

@Composable
private fun MainScreen.ScreenContent() {
    val model = rememberMainScreenModel()
    val sessions by model.sessions.collectAsState(listOf())

    Box(
        modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colors.background).padding(
            start = LocalSafeAreaInsets.current.leftDp(8f),
            end = LocalSafeAreaInsets.current.rightDp(8f),
        ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val titleText = model.getWrappedTitleText(string(Strings.MainScreenAllSessions))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = titleText,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                    style = MaterialTheme.typography.h5,
                )
                Row(
                    modifier = Modifier.padding(end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SearchBar(
                        text = model.searchText,
                        onTextChanged = { model.searchText = it },
                    )
                    SortingButton(
                        initialMethod = model.sortingMethod,
                        onMethodChanged = { model.sortingMethod = it },
                        allowedMethods = model.allowedSortingMethods,
                    )
                }
            }
            ItemDivider()
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                ScrollableLazyColumn {
                    items(sessions, key = { it }) { item ->
                        model.ItemRow(item.name, model::openSession) {
                            Text(item.name)
                        }
                        ItemDivider()
                    }
                }
                if (sessions.isEmpty()) {
                    Text(
                        text = if (model.hasSessions()) {
                            string(Strings.CommonNoMatch)
                        } else {
                            string(Strings.MainScreenEmpty)
                        },
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    )
                }
            }
        }
        Fab(model)
    }
}

@Composable
private fun ItemDivider() {
    Divider(modifier = Modifier.padding(start = 16.dp))
}

@Composable
private fun BoxScope.Fab(model: MainScreenModel) {
    val navigator = LocalNavigator.currentOrThrow
    FloatingActionButtonWrapper(
        model = model,
        icon = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = string(Strings.MainScreenNewSession),
            )
        },
        onClick = { navigator push CreateSessionReclistScreen },
    )
}
