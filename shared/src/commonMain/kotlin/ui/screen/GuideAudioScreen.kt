package ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.LocalFileInteractor
import kotlinx.coroutines.flow.collectLatest
import model.Action
import model.Actions
import repository.LocalAppActionStore
import repository.LocalGuideAudioRepository
import ui.common.ActionButtonWrapper
import ui.common.ActionMenu
import ui.common.ActionMenuItem
import ui.common.FloatingActionButton
import ui.common.ItemListScreenContent
import ui.common.LocalAlertDialogController
import ui.common.LocalToastController
import ui.model.LocalAppContext
import ui.model.LocalSafeAreaInsets
import ui.model.Screen
import ui.string.*

data class GuideAudioScreen(val sessionName: String) : Screen {
    @Composable
    override fun getTitle(): String = string(Strings.GuideAudioScreenTitle)

    @Composable
    override fun Content() = ScreenContent()

    @Composable
    override fun Actions() = ScreenActions()
}

@Composable
private fun GuideAudioScreen.ScreenActions() {
    val model = rememberGuideAudioScreenModel()
    ActionButtonWrapper(model) {
        ActionMenu { closeMenu ->
            val fileInteractor = LocalFileInteractor.current
            val repository = LocalGuideAudioRepository.current
            val alertDialogController = LocalAlertDialogController.current
            val toastController = LocalToastController.current
            val context = LocalAppContext.current
            ActionMenuItem(
                text = string(Strings.CommonImport),
                icon = Icons.Default.Add,
                onClick = {
                    closeMenu()
                    Actions.importGuideAudio(
                        context.coroutineScope,
                        fileInteractor,
                        repository,
                        alertDialogController,
                        toastController,
                    )
                },
            )
            ActionMenuItem(
                text = string(Strings.CommonEdit),
                icon = Icons.Default.Edit,
                onClick = {
                    closeMenu()
                    model.startEditing()
                },
            )
        }
    }
}

@Composable
private fun GuideAudioScreen.ScreenContent() {
    val model = rememberGuideAudioScreenModel()
    val navigator = LocalNavigator.currentOrThrow
    val appActionStore = LocalAppActionStore.current

    LaunchedEffect(appActionStore) {
        appActionStore.actions.collectLatest {
            when (it) {
                Action.EditList -> model.startEditing()
                Action.Exit -> navigator.pop()
                else -> Unit
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colors.background).padding(
            start = LocalSafeAreaInsets.current.leftDp(8f),
            end = LocalSafeAreaInsets.current.rightDp(8f),
        ),
    ) {
        ItemListScreenContent(model, string(Strings.GuideAudioScreenAllGuideAudios))
        FloatingActionButton(model)
    }
}
