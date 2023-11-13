package ui.common

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ui.string.*

@Composable
fun ActionMenu(content: @Composable ColumnScope.(closeMenu: () -> Unit) -> Unit) {
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
        content { showMenu = false }
    }
}
