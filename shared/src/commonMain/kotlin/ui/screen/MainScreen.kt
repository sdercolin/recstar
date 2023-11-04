package ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import const.APP_NAME
import ui.common.ScrollableLazyColumn
import ui.common.plainClickable
import ui.model.Screen
import ui.screen.demo.DemoShowcaseScreen
import ui.string.*
import util.alpha

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
    if (model.isSelectingForDeletion) {
        TextButton(
            onClick = { model.cancelSelectingForDeletion() },
        ) {
            Text(text = string(Strings.CommonCancel))
        }
    } else {
        var showMenu by remember { mutableStateOf(false) }
        IconButton(
            onClick = { showMenu = !showMenu },
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = string(Strings.CommonMore),
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            DropdownMenuItem(
                onClick = {
                    showMenu = false
                    model.startSelectingForDeletion()
                },
            ) {
                Text(text = string(Strings.CommonEdit))
            }
            DropdownMenuItem(onClick = { navigator push DemoShowcaseScreen }) {
                Text(text = "Demo showcase")
            }
        }
    }
}

@Composable
private fun MainScreen.ScreenContent() {
    val model = rememberMainScreenModel()
    val sessions by model.sessions.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colors.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            val titleText = if (model.isSelectingForDeletion) {
                string(Strings.MainScreenItemSelecting, model.selectedSessions.size)
            } else {
                string(Strings.MainScreenAllSessions)
            }
            Text(
                text = titleText,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                style = MaterialTheme.typography.h5,
            )
            ItemDivider()
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                ScrollableLazyColumn {
                    items(sessions, key = { it }) {
                        SessionItem(
                            item = it,
                            onClick = model::openSession,
                            isSelectable = model.isSelectingForDeletion,
                            isSelected = it in model.selectedSessions,
                            onSelect = model::selectForDeletion,
                        )
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
        FabBar(model)
    }
}

@Composable
private fun SessionItem(
    item: String,
    onClick: (String) -> Unit,
    isSelectable: Boolean,
    isSelected: Boolean,
    onSelect: (String, Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .run {
                if (isSelectable) {
                    plainClickable { onSelect(item, !isSelected) }
                } else {
                    clickable { onClick(item) }
                }
            }
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        if (isSelectable) {
            val tint = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.alpha(0.3f)
            Icon(
                modifier = Modifier.padding(end = 16.dp).size(20.dp),
                imageVector = Icons.Default.CheckCircle,
                contentDescription = string(Strings.CommonCheck),
                tint = tint,
            )
        }
        Text(
            text = item,
        )
    }
}

@Composable
private fun ItemDivider() {
    Divider(modifier = Modifier.padding(start = 16.dp))
}

@Composable
private fun BoxScope.FabBar(model: MainScreenModel) {
    val navigator = LocalNavigator.currentOrThrow
    if (!model.isSelectingForDeletion) {
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(32.dp),
            backgroundColor = MaterialTheme.colors.primary,
            onClick = { navigator push CreateSessionReclistScreen },
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = string(Strings.MainScreenNewSession),
            )
        }
    } else {
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(32.dp),
            backgroundColor = MaterialTheme.colors.secondaryVariant,
            onClick = { model.deleteSelectedSessions() },
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = string(Strings.CommonOkay),
            )
        }
    }
}
