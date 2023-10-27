package ui.screen.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.Paths
import io.logsDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import ui.model.LocalAppContext
import ui.model.Screen

object ExceptionDemoScreen : Screen {
    override val title: String
        get() = "ExceptionDemo"

    @Composable
    override fun Content() = ExceptionDemo()
}

@Composable
private fun ExceptionDemo() {
    val context = LocalAppContext.current
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = {
                throw Exception("Exception tested in composition")
            },
        ) {
            Text(text = "Throw Exception on UI Thread")
        }
        Button(
            onClick = {
                context.coroutineScope.launch {
                    throw Exception("Exception tested in default coroutine scope")
                }
            },
        ) {
            Text(text = "Throw Exception on Default Coroutine Scope")
        }
        Button(
            onClick = {
                context.coroutineScope.launch(Dispatchers.IO) {
                    throw Exception("Exception tested in IO coroutine scope")
                }
            },
        ) {
            Text(text = "Throw Exception on IO Coroutine Scope")
        }
        Button(
            onClick = {
                context.requestOpenFolder(Paths.logsDirectory)
            },
        ) {
            Text("Open log position")
        }
    }
}
