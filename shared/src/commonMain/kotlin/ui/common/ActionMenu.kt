package ui.common

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ui.string.*
import util.useIosStyle

@Composable
fun ActionMenu(content: @Composable ColumnScope.(closeMenu: () -> Unit) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    IconButton(
        onClick = { showMenu = !showMenu },
    ) {
        Icon(
            imageVector = if (useIosStyle) Icons.Default.MoreHoriz else Icons.Default.MoreVert,
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

@Composable
fun ActionMenuItem(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    DropdownMenuItem(onClick = onClick) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = text,
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(text = text)
    }
}
