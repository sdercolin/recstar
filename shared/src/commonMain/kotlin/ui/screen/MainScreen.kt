package ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Token
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
import repository.LocalSessionRepository
import ui.common.FreeSizedIconButton
import ui.common.ScrollableLazyColumn
import ui.model.Screen
import ui.screen.demo.DemoShowcaseScreen
import ui.string.*
import util.Log

object MainScreen : Screen {
    @Composable
    override fun getTitle(): String = APP_NAME

    @Composable
    override fun Actions() {
        val navigator = LocalNavigator.currentOrThrow
        IconButton(onClick = { navigator push DemoShowcaseScreen }) {
            Icon(
                imageVector = Icons.Default.Token,
                contentDescription = "Feature Demos",
            )
        }
    }

    @Composable
    override fun Content() = MainScreenContent()
}

@Composable
private fun MainScreenContent() {
    val repository = LocalSessionRepository.current
    val navigator = LocalNavigator.currentOrThrow
    val sessions by repository.items.collectAsState()
    LaunchedEffect(Unit) {
        repository.fetch()
    }

    fun openSession(name: String) {
        val session = repository.get(name)
            .getOrElse {
                Log.e("Failed to get session $name", it)
                return
            }
        navigator push SessionScreen(session)
    }

    Column(modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colors.background)) {
        Text(
            text = string(Strings.MainScreenAllSessions),
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
            style = MaterialTheme.typography.h4,
        )
        ItemDivider()
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            ScrollableLazyColumn {
                items(sessions, key = { it }) {
                    SessionItem(it, ::openSession)
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
        BottomBar()
    }
}

@Composable
private fun SessionItem(
    item: String,
    onClick: (String) -> Unit,
) {
    Text(
        text = item,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick(item) }
            .padding(horizontal = 16.dp, vertical = 24.dp),
    )
}

@Composable
private fun ItemDivider() {
    Divider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun BottomBar() {
    val navigator = LocalNavigator.currentOrThrow
    Row(
        modifier = Modifier.fillMaxWidth().background(color = MaterialTheme.colors.surface).padding(32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        FreeSizedIconButton(
            modifier = Modifier.size(64.dp),
            onClick = { navigator push CreateSessionReclistScreen },
        ) {
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = Icons.Default.Add,
                contentDescription = string(Strings.MainScreenNewSession),
                tint = MaterialTheme.colors.primary,
            )
        }
    }
}
