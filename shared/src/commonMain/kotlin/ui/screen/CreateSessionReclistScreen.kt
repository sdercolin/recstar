package ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import model.createSession
import ui.common.LocalAlertDialogController
import ui.common.ScrollableLazyColumn
import ui.common.requestConfirm
import ui.model.LocalAppContext
import ui.model.Screen
import ui.string.*
import util.Log

object CreateSessionReclistScreen : Screen {
    @Composable
    override fun getTitle(): String = string(Strings.CreateSessionReclistScreenTitle)

    @Composable
    override fun Content() = ScreenContent()

    @Composable
    override fun Actions() = ImportButton()
}

@Composable
private fun ImportButton() {
    val appContext = LocalAppContext.current
    IconButton(
        onClick = {
            appContext.pickFile(
                title = stringStatic(Strings.CreateSessionReclistScreenActionImport),
                allowedExtensions = listOf("txt"),
                onFinish = { file ->
                    file ?: return@pickFile
                    appContext.reclistRepository.import(file)
                },
            )
        },
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = string(Strings.CommonBack),
        )
    }
}

@Composable
private fun ScreenContent() {
    val context = LocalAppContext.current
    val alertDialogController = LocalAlertDialogController.current
    val items = context.reclistRepository.items.collectAsState()
    val navigator = LocalNavigator.current

    fun next(reclistName: String) {
        val reclist = context.reclistRepository.get(reclistName)
        val session = createSession(reclist)
            .onFailure {
                Log.e(it)
                alertDialogController.requestConfirm(
                    message = stringStatic(Strings.CreateSessionReclistScreenFailure),
                )
            }
            .getOrNull() ?: return
        navigator?.pop()
        navigator?.push(SessionScreen(session))
    }
    LaunchedEffect(context) {
        context.reclistRepository.fetch()
    }
    ScrollableLazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items.value, key = { it }) { name ->
            ReclistItem(name, ::next)
        }
    }
}

@Composable
private fun ReclistItem(
    name: String,
    onClick: (String) -> Unit,
) {
    Text(
        text = name,
        modifier = Modifier.fillMaxWidth()
            .clickable { onClick(name) }
            .padding(24.dp),
    )
}
