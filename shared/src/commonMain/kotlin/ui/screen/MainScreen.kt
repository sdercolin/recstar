package ui.screen

import LocalAppActionStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
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
import io.LocalFileInteractor
import kotlinx.coroutines.flow.collectLatest
import model.Action
import model.Actions
import repository.LocalReclistRepository
import ui.common.ActionMenu
import ui.common.ActionMenuItem
import ui.common.FloatingActionButtonWrapper
import ui.common.LocalToastController
import ui.common.ScrollableLazyColumn
import ui.model.Screen
import ui.screen.demo.DemoShowcaseScreen
import ui.string.*

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
    val fileInteractor = LocalFileInteractor.current
    val repository = LocalReclistRepository.current
    val toastController = LocalToastController.current
    LaunchedEffect(appActionStore, model) {
        appActionStore.actions.collectLatest {
            when (it) {
                Action.NewSession -> navigator push CreateSessionReclistScreen
                Action.ImportReclist -> Actions.importReclist(fileInteractor, repository, toastController)
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
                text = "Dev Tools",
                icon = Icons.Default.Code,
                onClick = { navigator push DemoShowcaseScreen },
            )
        }
    }
}

@Composable
private fun MainScreen.ScreenContent() {
    val model = rememberMainScreenModel()
    val sessions by model.sessions.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colors.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            val titleText = model.getWrappedTitleText(string(Strings.MainScreenAllSessions))
            Text(
                text = titleText,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                style = MaterialTheme.typography.h5,
            )
            ItemDivider()
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                ScrollableLazyColumn {
                    items(sessions, key = { it }) {
                        model.ItemRow(it, model::openSession) {
                            Text(it)
                        }
                        ItemDivider()
                    }
                }
                if (sessions.isEmpty()) {
                    Text(
                        text = string(Strings.MainScreenEmpty),
                        modifier = Modifier.align(Alignment.Center),
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
