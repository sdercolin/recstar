package ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import model.SortingMethod
import ui.string.*

@Composable
fun SortingButton(
    modifier: Modifier = Modifier,
    initialMethod: SortingMethod,
    onMethodChanged: (SortingMethod) -> Unit,
    allowedMethods: List<SortingMethod> = SortingMethod.entries,
) {
    var currentMethod by remember { mutableStateOf(initialMethod) }
    var isMenuOpen by remember { mutableStateOf(false) }
    IconButton(
        modifier = modifier,
        onClick = { isMenuOpen = true },
    ) {
        Icon(
            imageVector = Icons.Default.Sort,
            contentDescription = string(Strings.SortingMethod),
        )
        DropdownMenu(
            expanded = isMenuOpen,
            onDismissRequest = { isMenuOpen = false },
        ) {
            allowedMethods.forEach { method ->
                DropdownMenuItem(
                    onClick = {
                        currentMethod = method
                        onMethodChanged(method)
                        isMenuOpen = false
                    },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(method.getText())
                        if (method == currentMethod) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp).padding(start = 8.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
