package ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import io.LocalFileInteractor
import kotlinx.coroutines.flow.collectLatest
import model.Action
import model.Actions
import repository.LocalAppActionStore
import repository.LocalReclistRepository
import ui.common.ActionMenu
import ui.common.ActionMenuItem
import ui.common.FloatingActionButton
import ui.common.LocalAlertDialogController
import ui.common.LocalToastController
import ui.common.ScrollableLazyColumn
import ui.model.LocalSafeAreaInsets
import ui.model.Screen
import ui.string.*

object CreateSessionReclistScreen : Screen {
    @Composable
    override fun getTitle(): String = string(Strings.CreateSessionReclistScreenTitle)

    @Composable
    override fun Content() = ScreenContent()

    @Composable
    override fun Actions() = ScreenActions()
}

@Composable
private fun CreateSessionReclistScreen.ScreenActions() {
    val model = rememberCreateSessionReclistScreenModel()
    model.ActionButtonWrapper {
        ActionMenu { closeMenu ->
            val fileInteractor = LocalFileInteractor.current
            val repository = LocalReclistRepository.current
            val alertDialogController = LocalAlertDialogController.current
            val toastController = LocalToastController.current
            ActionMenuItem(
                text = string(Strings.CommonImport),
                icon = Icons.Default.Add,
                onClick = {
                    closeMenu()
                    Actions.importReclist(fileInteractor, repository, alertDialogController, toastController)
                },
            )
            ActionMenuItem(
                text = string(Strings.CommonEdit),
                icon = Icons.Default.Edit,
                onClick = {
                    closeMenu()
                    model.startSelectingForDeletion()
                },
            )
        }
    }
}

@Composable
private fun CreateSessionReclistScreen.ScreenContent() {
    val model = rememberCreateSessionReclistScreenModel()
    val reclists by model.reclists.collectAsState()
    val navigator = LocalNavigator.currentOrThrow
    val appActionStore = LocalAppActionStore.current

    LaunchedEffect(appActionStore) {
        appActionStore.actions.collectLatest {
            when (it) {
                Action.EditList -> model.startSelectingForDeletion()
                Action.Exit -> navigator.pop()
                else -> Unit
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize()
            .background(color = MaterialTheme.colors.background)
            .padding(
                start = LocalSafeAreaInsets.current.leftDp(8f),
                end = LocalSafeAreaInsets.current.rightDp(8f),
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val titleText = model.getWrappedTitleText(string(Strings.CreateSessionReclistScreenAllReclists))
            Text(
                text = titleText,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                style = MaterialTheme.typography.h5,
            )
            ItemDivider()
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                ScrollableLazyColumn {
                    items(reclists, key = { it }) { name ->
                        model.ItemRow(name, model::select) {
                            Text(name)
                        }
                        ItemDivider()
                    }
                }
                if (reclists.isEmpty()) {
                    Text(
                        text = string(Strings.CreateSessionReclistScreenEmpty),
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    )
                }
            }
        }
        FloatingActionButton(model)
    }
}

@Composable
private fun ItemDivider() {
    Divider(modifier = Modifier.padding(start = 16.dp))
}
