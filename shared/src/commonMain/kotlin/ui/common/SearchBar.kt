package ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import ui.string.*

@Composable
fun SearchBar(
    text: String,
    onTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(text.isNotEmpty()) }
    val focusRequester = remember { FocusRequester() }
    Row(modifier = modifier) {
        if (isExpanded) {
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
            var hasFocus by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.width(150.dp)
                    .background(color = MaterialTheme.colors.surface, shape = MaterialTheme.shapes.small)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                        .padding(4.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            if (hasFocus && it.hasFocus.not() && text.isEmpty()) {
                                isExpanded = false
                            }
                            hasFocus = it.hasFocus
                        },
                    textStyle = TextStyle(color = MaterialTheme.colors.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colors.onSurface),
                )
                FreeSizedIconButton(
                    modifier = Modifier.size(16.dp),
                    onClick = {
                        isExpanded = false
                        onTextChanged("")
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = string(Strings.SearchBarClear),
                    )
                }
            }
        } else {
            IconButton(onClick = { isExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = string(Strings.SearchBar),
                )
            }
        }
    }
}
