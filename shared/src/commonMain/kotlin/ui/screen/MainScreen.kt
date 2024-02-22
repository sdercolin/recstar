package ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import const.APP_NAME
import kotlinx.coroutines.flow.collectLatest
import model.Action
import repository.LocalAppActionStore
import ui.common.ActionButtonWrapper
import ui.common.ActionMenu
import ui.common.ActionMenuItem
import ui.common.FloatingActionButton
import ui.common.ItemListScreenContent
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
                Action.EditList -> model.startEditing()
                else -> Unit
            }
        }
    }
    ActionButtonWrapper(model) {
        ActionMenu { closeMenu ->
            ActionMenuItem(
                text = string(Strings.CommonEdit),
                icon = Icons.Default.Edit,
                onClick = {
                    closeMenu()
                    model.startEditing()
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
    val navigator = LocalNavigator.currentOrThrow
    Box(
        modifier = Modifier.fillMaxSize()
            .background(color = MaterialTheme.colors.background)
            .padding(
                start = LocalSafeAreaInsets.current.leftDp(8f),
                end = LocalSafeAreaInsets.current.rightDp(8f),
            ),
    ) {
        ItemListScreenContent(model, string(Strings.MainScreenAllSessions))
        FloatingActionButton(
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
}
