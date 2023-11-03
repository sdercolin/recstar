package ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import io.LocalFileInteractor
import repository.LocalReclistRepository
import repository.LocalSessionRepository
import ui.common.LocalAlertDialogController
import ui.common.ScrollableLazyColumn
import ui.common.requestConfirm
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
    val repository = LocalReclistRepository.current
    val fileInteractor = LocalFileInteractor.current
    IconButton(
        onClick = {
            fileInteractor.pickFile(
                title = stringStatic(Strings.CreateSessionReclistScreenActionImport),
                allowedExtensions = listOf("txt"),
                onFinish = { file ->
                    file ?: return@pickFile
                    repository.import(file)
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
    val alertDialogController = LocalAlertDialogController.current
    val reclistRepository = LocalReclistRepository.current
    val sessionRepository = LocalSessionRepository.current
    val reclists by reclistRepository.items.collectAsState()
    val navigator = LocalNavigator.current

    fun next(reclistName: String) {
        val reclist = reclistRepository.get(reclistName)
        val session = sessionRepository.create(reclist)
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
    LaunchedEffect(reclistRepository) {
        reclistRepository.fetch()
    }
    Box(modifier = Modifier.fillMaxWidth().background(color = MaterialTheme.colors.background)) {
        ScrollableLazyColumn(modifier = Modifier.fillMaxSize()) {
            items(reclists, key = { it }) { name ->
                ReclistItem(name, ::next)
                Divider()
            }
        }
        if (reclists.isEmpty()) {
            Text(
                text = string(Strings.CreateSessionReclistScreenEmpty),
                modifier = Modifier.align(Alignment.Center),
            )
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(name) }
            .padding(horizontal = 24.dp, vertical = 24.dp),
    )
}
