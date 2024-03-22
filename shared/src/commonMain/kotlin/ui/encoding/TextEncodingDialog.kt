package ui.encoding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.readTextWithEncodingOrNull
import ui.common.DialogContainer
import ui.common.VerticalScrollbar
import ui.string.*
import util.Encoding
import util.Log
import util.isDesktop

@Composable
fun TextEncodingDialog(
    request: TextEncodingDialogRequest,
    submit: (Encoding?) -> Unit,
    dismiss: () -> Unit,
) {
    var encoding by remember(request) { mutableStateOf(request.currentEncoding) }
    val text = produceState<Result<String>?>(null, encoding) {
        value = runCatching { request.file.readTextWithEncodingOrNull(encoding) }
            .onFailure { Log.e(it) }
    }
    DialogContainer(
        fraction = if (isDesktop) 0.7f else 0.8f,
        onClickOutside = {},
    ) {
        Column {
            Column(
                Modifier.weight(1f).padding(
                    top = 24.dp,
                    start = 24.dp,
                    end = 24.dp,
                ),
            ) {
                Text(string(Strings.TextEncodingDialogTitle), style = MaterialTheme.typography.body1)
                Spacer(modifier = Modifier.height(16.dp))
                EncodingSelector(encoding) { encoding = it }
                Spacer(modifier = Modifier.height(16.dp))
                val scrollState = rememberScrollState()
                Box(
                    Modifier.fillMaxWidth().weight(1f)
                        .background(color = MaterialTheme.colors.background)
                        .padding(16.dp),
                ) {
                    val textResult = text.value
                    Text(
                        modifier = Modifier.verticalScroll(scrollState),
                        text = when {
                            textResult == null -> ""
                            textResult.isFailure -> string(Strings.TextEncodingDialogEncodingError)
                            textResult.isSuccess -> textResult.getOrNull() ?: ""
                            else -> ""
                        },
                        color = if (textResult?.isFailure == true) {
                            MaterialTheme.colors.error
                        } else {
                            MaterialTheme.colors.onBackground
                        },
                    )
                    if (isDesktop) {
                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            scrollState = scrollState,
                        )
                    }
                    if (text.value == null) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
            ) {
                TextButton(
                    onClick = dismiss,
                ) {
                    Text(string(Strings.CommonCancel))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    enabled = text.value?.isSuccess == true,
                    onClick = { submit(encoding) },
                ) {
                    Text(string(Strings.CommonOkay), style = MaterialTheme.typography.body2)
                }
            }
        }
    }
}

@Composable
private fun EncodingSelector(
    encoding: Encoding?,
    onChange: (Encoding?) -> Unit,
) {
    Box {
        var expanded by remember { mutableStateOf(false) }
        TextField(
            label = { Text(string(Strings.TextEncodingDialogEncodingLabel)) },
            modifier = Modifier.widthIn(min = 400.dp),
            value = encoding?.value ?: string(Strings.TextEncodingDialogEncodingAuto),
            onValueChange = { },
            readOnly = true,
            singleLine = true,
            leadingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ExpandMore, null)
                }
            },
        )
        DropdownMenu(
            modifier = Modifier.align(Alignment.CenterEnd),
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                onClick = {
                    onChange(null)
                    expanded = false
                },
            ) {
                Text(text = string(Strings.TextEncodingDialogEncodingAuto))
            }
            Encoding.entries.forEach { item ->
                DropdownMenuItem(
                    onClick = {
                        onChange(item)
                        expanded = false
                    },
                ) {
                    Text(text = item.value)
                }
            }
        }
    }
}
